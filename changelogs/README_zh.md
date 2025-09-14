## 变更日志条目

#### 概述

`unreleased` 文件夹包含所有尚未发布的变更日志条目。

在发布时，所有未发布的条目将合并为一个最终的 `CHANGELOG.md` 文件。

#### 变更日志条目

在 `changelogs/unreleased` 下创建一个新文件。

该文件应为以下格式的 YAML 文件：

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

`title` 简要总结了对任何用户的影响。

`module` 是模块的技术名称（文件夹名称/gradle id），即发生更改的模块。

`developer` 部分提供了可能影响依赖于 Axelor Open Suite 模块的自定义模块的技术细节。
例如，构造函数更改、视图或菜单修改应包括在此部分中。

### 变更日志标题应关注应用程序使用

**不要在“developer”部分之外使用技术字段名称。** 必须写明此更改对应用程序用户的后果。

有关更改的任何技术信息可以根据需要放在提交消息或“developer”消息中，但应避免出现在标题中。

例如，不要写：

> 发票：当状态选择等于 `STATUS_CANCELED` 时，将类型选择的隐藏属性设置为 true。

而是写：

> 发票：修复了取消发票中显示类型的错误。

#### 生成 CHANGELOG.md

要使用未发布的条目生成 `CHANGELOG.md`，请运行以下 gradle 任务：
```
./gradlew generateChangeLog
```

未发布的条目也会自动从 `changelogs/unreleased` 中删除。

可以使用 `--preview` 参数预览生成的变更日志而不删除或更新文件。

#### 来源

* [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
* [Gitlab: 如何解决CHANGELOG冲突](https://about.gitlab.com/2018/07/03/solving-gitlabs-changelog-conflict-crisis/)