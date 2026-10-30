# Cloud Shell operators for the 1.23 delivery

These are the exact temporary operator inputs used for the existing Firebase
site. Their original names on the owned Cloud Shell VM were
`/tmp/luma123-cloud-run.py` and `/tmp/luma123-cloud-finalize.py`. The byte hashes
are recorded by the original operation receipts. The checked-in filenames do
not change their bytes.

The first operator stages an immutable source commit in a new sparse checkout
and reconstructs the exact two signed APKs from hash-pinned 1.22 bases. The
second validates that existing STAGED_ONLY workspace, then invokes its frozen
REST deployer. OAuth is acquired in memory and passed only through stdin.
Original staging records and distinct finalization records are preserved.

The offline validation JSONs describe local Git/fictional-provider tests; they
are separate from the actual Cloud Shell and Hosting receipts. These operators
are pinned to this release and its accepted 1.22 baseline, not a general-purpose
deployment command for future versions.
