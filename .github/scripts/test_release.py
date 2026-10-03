import json
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import release


BADGING = """package: name='com.charlyghislain.openopenradio' versionCode='12' versionName='1.2.0'
minSdkVersion:'24'
"""


class ReleaseTests(unittest.TestCase):
    def test_tag_must_match_apk_version_and_package(self):
        self.assertEqual(release.apk_version(BADGING, "v1.2.0"), ("1.2.0", 12))
        for badging, tag in [
            (BADGING, "v1.2.1"),
            (BADGING.replace(release.APPLICATION_ID, "another.app"), "v1.2.0"),
            (BADGING.replace("minSdkVersion:'24'", "minSdkVersion:'23'"), "v1.2.0"),
            (BADGING.replace("versionCode='12'", "versionCode='0'"), "v1.2.0"),
        ]:
            with self.subTest(badging=badging, tag=tag), self.assertRaises(ValueError):
                release.apk_version(badging, tag)

    def test_tag_names_cannot_be_paths_or_prereleases(self):
        for tag in ["master", "v1.2.0-beta", "v1.2/../file", "v1.2;false"]:
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                release.tag_version(tag)

    def test_tag_must_reference_checked_out_commit(self):
        with tempfile.TemporaryDirectory() as directory:
            def git(*args):
                return subprocess.run(["git", "-C", directory, *args], check=True,
                                      capture_output=True, text=True)
            git("init")
            git("config", "user.email", "test@example.org")
            git("config", "user.name", "Release test")
            git("commit", "--allow-empty", "-m", "Tagged commit")
            git("tag", "-a", "v1.2.0", "-m", "Version 1.2.0")
            self.assertEqual(release.check_tag("v1.2.0", Path(directory)),
                             git("rev-parse", "HEAD").stdout.strip())
            git("commit", "--allow-empty", "-m", "Later commit")
            with self.assertRaises(ValueError):
                release.check_tag("v1.2.0", Path(directory))

    def test_changed_unsigned_artifact_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            apk = output / "unsigned.apk"
            apk.write_bytes(b"expected APK")
            data = dict(tag="v1.2.0", version="1.2.0", filename="openopenradio-1.2.0.apk",
                        unsigned_sha256=release.digest(apk))
            (output / "release.json").write_text(json.dumps(data))
            self.assertEqual(release.metadata(output), data)
            apk.write_bytes(b"changed APK")
            with self.assertRaises(ValueError):
                release.metadata(output)

    def test_wrong_signing_identity_is_rejected(self):
        result = subprocess.CompletedProcess([], 0, "Signer #1 certificate SHA-256 digest: " + "0" * 64)
        with patch.object(release, "run", return_value=result), self.assertRaises(ValueError):
            release.verify_signature("apksigner", "signed.apk")

    def test_retries_do_not_overwrite_a_different_asset(self):
        asset = dict(name="openopenradio-1.2.0.apk", digest="sha256:" + "a" * 64)
        release.verify_existing_asset("v1.2.0", asset, "a" * 64)
        with self.assertRaises(ValueError):
            release.verify_existing_asset("v1.2.0", asset, "b" * 64)

    def test_published_asset_mismatch_prevents_all_writes(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            unsigned = output / "unsigned.apk"
            unsigned.write_bytes(b"unsigned APK")
            apk = output / "openopenradio-1.2.0.apk"
            apk.write_bytes(b"signed APK")
            checksum = release.digest(apk)
            (output / f"{apk.name}.sha256").write_text(f"{checksum}  {apk.name}\n")
            data = dict(tag="v1.2.0", version="1.2.0", filename=apk.name,
                        unsigned_sha256=release.digest(unsigned))
            (output / "release.json").write_text(json.dumps(data))
            existing = dict(isDraft=False, assets=[dict(name=apk.name, digest="sha256:" + "0" * 64)])
            view = subprocess.CompletedProcess([], 0, json.dumps(existing))
            with patch.object(release.subprocess, "run", return_value=view), \
                    patch.object(release, "verify_signature"), \
                    patch.object(release, "sdk_tool", return_value="apksigner"), \
                    patch.object(release, "run") as writes:
                with self.assertRaises(ValueError):
                    release.publish(output)
                writes.assert_not_called()


if __name__ == "__main__":
    unittest.main()
