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

#### How to know whether a new changelog entry is needed

**Except in some rare cases, you do not have to add a changelog entry
for a fix made in a wip branch**.

A changelog needs to list the change from previous version to current
version. When the version is in development, new features are creating
regression. If we fix these regressions before the version is released,
then these bugs will never appear on any released version, so the fix
stays out of the changelog. Now if we are fixing a released version in
which the bug appeared, then we must add the changelog entry.


### How to choose the section of the entry

-   **If you are on a dev branch, most of the time the type is either
    `fix` or `change`.**
-   **If you are on a wip branch, the type can be `change` or `feature`
    (see above).**

A `change` is current "Improvements" section: adding a simple field in
existing class is not a feature, but a change.

Special cases of feature in dev branch and fix in wip can exist, but it
is supposed to happen only in rare cases.

### Changelog is about application usage

**Do not use technical field name.** You must write what is the
consequence of the change for the user of the application.
Any technical information about the change can go if needed to the commit message, but should be avoided in the changelog.

For example, instead of

> Update hidden attribute of typeSelect to true when statusSelect is equal to `STATUS_CANCELED`.

write

> In invoice form view, hide the type for canceled invoices.

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
