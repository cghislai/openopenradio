"""Build metadata, sign reproducibly, and publish version-tagged APKs."""

import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile


APPLICATION_ID = "com.charlyghislain.openopenradio"
SIGNER_SHA256 = "5e137169c130744d3e9643d2b71367b1f94f14cc56933d592eb27d46c6c94a5e"


def run(*args, **kwargs):
    return subprocess.run(args, check=True, text=True, **kwargs)


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def tag_version(tag):
    if not re.fullmatch(r"v[0-9]+\.[0-9]+(?:\.[0-9]+)?", tag):
        raise ValueError("Use a stable version tag such as v1.2 or v1.2.3.")
    return tag[1:]


def check_tag(tag, source):
    tag_version(tag)
    head = run("git", "-C", str(source), "rev-parse", "HEAD", capture_output=True).stdout.strip()
    commit = run("git", "-C", str(source), "rev-parse", "--verify",
                 f"refs/tags/{tag}^{{commit}}", capture_output=True).stdout.strip()
    if head != commit:
        raise ValueError("The checkout must be the commit referenced by the release tag.")
    return commit


def sdk_tool(name, version):
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not sdk:
        raise ValueError("Set ANDROID_HOME to the Android SDK directory.")
    path = Path(sdk) / "build-tools" / version / name
    if not path.is_file():
        raise ValueError(f"Install Android SDK build-tools {version} ({name} is missing).")
    return str(path)


def apk_version(badging, tag):
    package = re.search(r"^package: name='([^']+)' versionCode='([0-9]+)' versionName='([^']+)'",
                        badging, re.MULTILINE)
    sdk = re.search(r"^(?:minSdkVersion|sdkVersion):'([0-9]+)'", badging, re.MULTILINE)
    if not package or not sdk:
        raise ValueError("Cannot read the APK's package, version, and minimum SDK.")
    app_id, code, version = package.groups()
    if app_id != APPLICATION_ID or version != tag_version(tag):
        raise ValueError("The APK package/version does not match the release tag.")
    if int(code) <= 0 or int(sdk[1]) < 24:
        raise ValueError("Expected a positive version code and minimum SDK >= 24 for v2 signing.")
    return version, int(code)


def prepare(tag, source, apk, output):
    commit = check_tag(tag, source)
    badging = run(sdk_tool("aapt2", "36.0.0"), "dump", "badging", str(apk),
                  capture_output=True).stdout
    version, code = apk_version(badging, tag)
    changelog = source / "fastlane/metadata/android/en-US/changelogs" / f"{code}.txt"
    if not changelog.is_file() or not changelog.read_text().strip():
        raise ValueError(f"Add release notes to {changelog} before tagging.")
    output.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(apk, output / "unsigned.apk")
    data = dict(tag=tag, version=version, version_code=code, commit=commit,
                filename=f"openopenradio-{version}.apk", unsigned_sha256=digest(apk),
                changelog=changelog.read_text().strip())
    (output / "release.json").write_text(json.dumps(data, indent=2) + "\n")
    print(f"Prepared {tag}, version code {code}, source {commit}.")


def metadata(output):
    data = json.loads((output / "release.json").read_text())
    version = tag_version(data["tag"])
    if data["version"] != version or data["filename"] != f"openopenradio-{version}.apk":
        raise ValueError("Invalid release artifact metadata.")
    if digest(output / "unsigned.apk") != data["unsigned_sha256"]:
        raise ValueError("The unsigned APK does not match its recorded checksum.")
    return data


def verify_signature(apksigner, apk):
    result = run(apksigner, "verify", "--verbose", "--print-certs", str(apk),
                 capture_output=True).stdout
    fingerprints = re.findall(r"^Signer #[0-9]+ certificate SHA-256 digest: ([0-9a-f]+)$",
                              result, re.MULTILINE)
    if fingerprints != [SIGNER_SHA256]:
        raise ValueError("The APK must use the established developer signing certificate.")


def sign(output):
    import apksigcopier

    data = metadata(output)
    for name in ("ANDROID_KEYSTORE_BASE64", "ANDROID_KEYSTORE_PASSWORD"):
        if not os.environ.get(name):
            raise ValueError(f"Configure the GitHub Actions secret {name}.")
    apksigner = sdk_tool("apksigner", "34.0.0")
    apk = output / data["filename"]
    with tempfile.TemporaryDirectory(prefix="apk-signing-") as directory:
        keystore = Path(directory) / "release.keystore"
        keystore.write_bytes(base64.b64decode(
            "".join(os.environ["ANDROID_KEYSTORE_BASE64"].split()), validate=True))
        keystore.chmod(0o600)
        env = os.environ.copy()
        env["ANDROID_KEY_PASSWORD"] = env.get("ANDROID_KEY_PASSWORD") or env["ANDROID_KEYSTORE_PASSWORD"]
        args = [apksigner, "sign", "--ks", str(keystore),
                "--ks-pass", "env:ANDROID_KEYSTORE_PASSWORD", "--key-pass", "env:ANDROID_KEY_PASSWORD",
                "--v1-signing-enabled", "false", "--v2-signing-enabled", "true",
                "--v3-signing-enabled", "true", "--out", str(apk)]
        if env.get("ANDROID_KEY_ALIAS"):
            args += ["--ks-key-alias", env["ANDROID_KEY_ALIAS"]]
        run(*args, str(output / "unsigned.apk"), env=env)
        verify_signature(apksigner, apk)
        copied = Path(directory) / "signature-copied.apk"
        apksigcopier.do_copy(str(apk), str(output / "unsigned.apk"), str(copied),
                             exclude=apksigcopier.exclude_meta)
        verify_signature(apksigner, copied)
    checksum = digest(apk)
    (output / f"{data['filename']}.sha256").write_text(f"{checksum}  {data['filename']}\n")
    notes = (f"{data['changelog']}\n\n"
             f"**Install:** download `{data['filename']}` below. Signed with the developer key.\n\n"
             f"Source commit: `{data['commit']}`\n\n"
             f"Signing certificate SHA-256: `{SIGNER_SHA256}`\n\n"
             f"APK SHA-256: `{checksum}`\n")
    (output / "release-notes.md").write_text(notes)
    print(f"Signature-copy verification passed. APK SHA-256: {checksum}")


def verify_existing_asset(tag, asset, expected):
    if asset.get("digest"):
        actual = asset["digest"].removeprefix("sha256:")
    else:
        with tempfile.TemporaryDirectory(prefix="release-asset-") as directory:
            run("gh", "release", "download", tag, "--pattern", asset["name"], "--dir", directory)
            actual = digest(Path(directory) / asset["name"])
    if actual != expected:
        raise ValueError(f"Existing release asset {asset['name']} differs; use a new version tag.")


def publish(output):
    data = metadata(output)
    apk = output / data["filename"]
    checksum_file = output / f"{data['filename']}.sha256"
    checksum = digest(apk)
    if checksum_file.read_text() != f"{checksum}  {data['filename']}\n":
        raise ValueError("The signed APK checksum is incorrect.")
    verify_signature(sdk_tool("apksigner", "34.0.0"), apk)
    tag = data["tag"]
    result = subprocess.run(["gh", "release", "view", tag, "--json", "isDraft,assets"],
                            text=True, capture_output=True)
    if result.returncode:
        if "release not found" not in result.stderr.lower():
            raise RuntimeError(result.stderr.strip())
        run("gh", "release", "create", tag, "--verify-tag", "--draft",
            "--title", f"Open Open Radio {data['version']}",
            "--notes-file", str(output / "release-notes.md"))
        release = {"isDraft": True, "assets": []}
    else:
        release = json.loads(result.stdout)
    assets = {a["name"]: a for a in release["assets"]}
    # Retrying a workflow must never replace a different, already released APK.
    for path in (apk, checksum_file):
        if path.name in assets:
            verify_existing_asset(tag, assets[path.name], digest(path))
        elif release["isDraft"] or path == checksum_file:
            run("gh", "release", "upload", tag, str(path))
        else:
            raise ValueError("This published release is missing its APK; refusing to rewrite it.")
    if release["isDraft"]:
        run("gh", "release", "edit", tag, "--draft=false", "--latest",
            "--notes-file", str(output / "release-notes.md"))
    print(f"Published {tag}: {data['filename']} ({checksum}).")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    for name in ("check-tag", "prepare"):
        command = commands.add_parser(name)
        command.add_argument("--tag", required=True)
        command.add_argument("--source", type=Path, default=Path.cwd())
        if name == "prepare":
            command.add_argument("--apk", type=Path, required=True)
            command.add_argument("--output", type=Path, required=True)
    for name in ("sign", "publish"):
        commands.add_parser(name).add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        if args.command == "check-tag":
            check_tag(args.tag, args.source)
        elif args.command == "prepare":
            prepare(args.tag, args.source, args.apk, args.output)
        elif args.command == "sign":
            sign(args.output)
        else:
            publish(args.output)
    except (ValueError, RuntimeError, subprocess.CalledProcessError) as error:
        parser.exit(1, f"Release failed: {error}\n")


if __name__ == "__main__":
    main()
