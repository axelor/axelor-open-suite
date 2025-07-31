# 贡献指南

我们非常欢迎您为我们的源代码做出贡献，使应用程序更加完善。
虽然我们尽量将贡献要求降到最低，
但还是有一些指南希望您能遵守：

* [贡献者许可协议](#contributor-license-agreement)
* [报告问题](#reporting-issues)
* [提交贡献](#submitting)
* [行为准则](#code-of-conduct)

## 贡献者许可协议

如果您以个人身份提交代码，则需同意[个人贡献者许可协议][individual-cla]。
如果您以实体身份提交代码，则需同意[企业贡献者许可协议][corporate-cla]。

## 安全问题

如果您认为自己发现了一个安全漏洞，请阅读我们的[安全策略](SECURITY.md)以获取更多详细信息。

## 报告问题

在提交问题之前，请先搜索存档，也许您的问题已经得到了解答。

如果您的问题看起来是一个错误，并且尚未被报告，请打开一个新问题。
帮助我们最大化用于修复问题和添加新功能的时间，
请不要报告重复的问题。提供以下信息将增加您的问题得到快速处理的机会：

* **使用场景** – 解释您的使用场景
* **问题概述** – 包括堆栈跟踪
* **版本** – 您正在使用哪个版本？
* **浏览器和操作系统** – 这是所有浏览器的问题吗？
* **重现错误** – 提供一个补丁来重现错误
* **相关问题** – 是否有类似的问题已被报告？
* **建议修复方案** – 您可以指出可能导致问题的代码行或提交

请确保在报告问题时不要发布任何敏感信息。

## 提交贡献

* [Fork](https://help.github.com/articles/fork-a-repo/) 仓库。
* 编码！
* 格式化 Java 代码以遵循 Google 代码格式。工具：
  * Gradle 任务: `./gradlew spotlessApply`
  * IDE 插件
    * [Eclipse](https://github.com/google/google-java-format#eclipse)
    * [IntelliJ](https://github.com/google/google-java-format#intellij)
* 您必须创建一个变更日志条目来描述更改。
  请参阅[changelogs 文件夹中的 README](https://github.com/axelor/axelor-open-suite/blob/master/changelogs/README.md)
  并按照说明操作。
* 将更改推送到您 fork 的仓库中的主题分支。

* 在开发分支上发起一个[pull request](http://help.github.com/send-pull-requests/)
  以解决问题。例如，如果问题出现在 `master` 分支，则选择 `dev` 分支，
  如果问题出现在 `5.3` 分支，则选择 `5.3-dev` 等...

## 行为准则

作为本项目的贡献者和维护者，为了营造一个开放和欢迎的社区，
我们承诺尊重所有通过报告问题、发布功能请求、更新文档、
提交 pull request 或补丁以及其他活动做出贡献的人。

我们致力于使参与本项目成为每个人的无骚扰体验，
无论其经验水平、性别、性别认同和表达、性取向、残疾、外貌、
体型、种族、民族、年龄、宗教或国籍如何。

参与者不可接受的行为示例包括：

* 使用带有性暗示的语言或图像
* 人身攻击
* 骚扰或侮辱/贬低性评论
* 公开或私下骚扰
* 发布他人的私人信息（如物理或电子地址），除非获得明确许可
* 其他不道德或不专业的行为

项目维护者有权和责任移除、编辑或拒绝不符合此行为准则的
评论、提交、代码、wiki 编辑、问题和其他贡献，
或暂时或永久禁止任何他们认为不合适、威胁、冒犯或有害的贡献者。

通过采用此行为准则，项目维护者承诺公平一致地将这些原则应用于管理项目的各个方面。
未遵守或执行行为准则的项目维护者可能会被永久从项目团队中移除。

此行为准则适用于项目空间内以及个人代表项目或其社区的公共空间。

有关滥用、骚扰或其他不可接受行为的实例可以通过联系项目维护者 [conduct@axelor.com][mail] 进行报告。所有投诉都将被审查和调查，并根据情况作出必要的和适当的回应。维护者有义务对事件举报人保密。

此行为准则是根据 [贡献者公约][homepage] 制定的，
版本 1.3.0，可在 [http://contributor-covenant.org/version/1/3/0/][version] 获取。

[mail]: mailto:conduct@axelor.com
[homepage]: http://contributor-covenant.org
[version]: http://contributor-covenant.org/version/1/3/0/
[individual-cla]: http://axelor.com/cla/individuel-cla/
[corporate-cla]: http://axelor.com/cla/corporate-cla/