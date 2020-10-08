## Changelog entries

#### Overview

The `unreleased` folder contains all changelog entries that haven't been release yet.

At the release time, all unreleased entries are combined into final CHANGELOG.md file. 

#### Change log entry

Under `changelogs/unreleased`, create a new file.

The file is expected to be a YAML file in the following format:

````yaml
---
title: Some text
type: feature
description: |
  some description here
  with more details.

  And some details about breaking changes
  and migrations steps.

  ```
  $ find -iregex ".*domains.*.xml" | xargs -l sed -i 's|cachable="|cacheable="|g'
  ```
````

The `title` describe the entry.

The `type` can be : 
* **feature** for new features.
* **change** for changes in existing functionality.
* **deprecate** for soon-to-be removed features.
* **remove** for now removed features.
* **fix** for any bug fixes.
* **security** in case of vulnerabilities.

The `description` is optional and should provide detail description about the changes including
migration steps if any.

#### Generate CHANGELOG.md

To generate the `CHANGELOG.md` with unreleased entries, run following gradle task:
```
./gradlew generateChangeLog
```

The unreleased entries are also automatically removed from `changelogs/unreleased`.

`--preview` arguments can also be used to preview the generated change log without deleting/updating files.

#### Source

* [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
* [Gitlab: How we solved GitLab's CHANGELOG conflict crisis](https://about.gitlab.com/2018/07/03/solving-gitlabs-changelog-conflict-crisis/)
