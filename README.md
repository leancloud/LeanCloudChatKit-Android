# ChatKit

ChatKit 是由 LeanCloud 官方推出的、基于 [LeanCloud 实时通信 SDK「LeanMessage」](https://leancloud.cn/docs/realtime_v2.html) 开发并封装了简单 UI  的聊天套件。它可以帮助开发者快速掌握 LeanMessage 的技术细节，轻松扩展和实现常用的聊天功能。

ChatKit 是一个免费且开源的项目组件，提供完全自由的授权协议，开发者可以对其进行任意的自定义和二次封装。ChatKit 的底层依然基于 LeanCloud 为各平台推出的 SDK，其最大特点是把聊天常用的一些功能配合 UI 一起提供给开发者。

使用方法请参考[官方文档](https://leancloud.cn/docs/chatkit-android.html)。

## 普通聊天消息
LeanCloud 即时通讯支持发送普通的文本、图片、音频、视频、文件和地理位置消息，支持直接发送二进制消息，并且还支持开发者扩展自己的消息类型。我们支持单聊、群聊、不限人数的开放聊天室，以及临时聊天室和系统对话（公众账号）。与其他 IM 服务不同的是，LeanCloud 即时通讯服务提供给你最大的灵活性和自由度，包括：
1. 内嵌离线推送与消息同步机制，可以保证离线消息以最快速度下发到客户端；
2. 支持多设备同时登录，且允许开发者自由选择合适的多点登录模式；
3. 支持第三方服务端进行安全签名与权限控制；
4. 消息生命周期的全过程都支持第三方 hook 函数，允许业务方进行深度定制；
5. 开放 API 可以在第三方服务端进行更多操作；

具体可以参考我们的开发指南：
1. [从简单的单聊、群聊、收发图文消息开始](https://leancloud.cn/docs/realtime-guide-beginner.html)
2. [消息收发的更多方式，离线推送与消息同步，多设备登录](https://leancloud.cn/docs/realtime-guide-intermediate.html)
3. [安全与签名、黑名单和权限管理、玩转聊天室和临时对话](https://leancloud.cn/docs/realtime-guide-senior.html)
4. [详解消息 hook 与系统对话，打造自己的聊天机器人](https://leancloud.cn/docs/realtime-guide-systemconv.html)

## 实时音视频聊天
LeanCloud 与声网是深度合作伙伴，推荐开发者接入声网 SDK 实现实时音视频通讯功能。
本 Demo 已经做了集成(参见分支：https://github.com/leancloud/LeanCloudChatKit-Android/tree/agora )，希望可以给大家提供一些参考价值。

