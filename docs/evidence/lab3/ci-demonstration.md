# Deliberate failure and restoration

Commit 3357e6d deliberately changed strict overlap comparisons to inclusive comparisons. This wrongly rejects adjacent visits. The unchanged server tests detected two failures locally and the server-tests job failed in https://github.com/Yazanleva/1.-officepulse/actions/runs/44841338752 .

The following correction restores strict comparisons. All eight server tests pass locally (restored-green.txt). This temporary broken implementation was confined to the draft PR branch and was never merged.

The main-branch ruleset now requires android-unit-tests and server-tests, up-to-date branches and one approving peer review, with no bypass actors. Final green CI evidence is recorded in the progress document.
