## Changelog entries

#### Overview

The `unreleased` folder contains all changelog entries that have not been released yet.

At the release time, all unreleased entries are combined into one final CHANGELOG.md file.

#### Change log entry

Under `changelogs/unreleased`, create a new file.

The file is expected to be a YAML file in the following format:

````yaml
---
title: Some text
module: axelor-base
developer: |
  some description here with technical details about breaking changes
  and migrations steps.

  ```
  $ find -iregex ".*domains.*.xml" | xargs -l sed -i 's|cachable="|cacheable="|g'
  ```
````

The `title` summarizes the change for any user.

The `module` is the technical name (the folder name/gradle id) of the module in which the change was made.

The `developer` section provides technical details about the changes that could
impact custom modules depending on Axelor Open Suite modules.
For example, a constructor change, a view or a menu modification should be included.

### Changelog title is about application usage

**Do not use technical field name outside of the "developer" section.** You must write what is the
consequence of the change for the user of the application.

Any technical information about the change can go if needed either to the
commit message or in the "developer" message, but should be avoided in the
title.

For example, instead of

> Invoice: fixed hidden attribute of typeSelect to true when statusSelect is equal to `STATUS_CANCELED`.

write

> Invoice: fixed an issue where the type was shown in canceled invoices.

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
