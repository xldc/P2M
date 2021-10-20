P2M
===
[![](https://jitpack.io/v/wangdaqi77/P2M.svg)](https://jitpack.io/#wangdaqi77/P2M) [![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)

一个简单、高效、安全、完整的Android组件化框架库。

支持环境:
 * AGP：3.4.0+
 * Gradle：6.1.1+

P2M是什么？
---------
P2M是完整的组件化工具，支持单独编译、单独运行、打包到仓库等主要功能，模块的服务、事件、启动器在模块内部无需做下沉处理，在运行时根据模块依赖关系进行安全的初始化模块。

P2M在Gradle中编译时主要将Project态升级为Module态。

Project态与Module态简易对比：
<div class="table-wrap">
    <table class="confluenceTable">
        <thead>
        <tr>
            <th class="confluenceTh">Project态</th>
            <th class="confluenceTh">Module态</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="confluenceTd">include ':lib-login'</td>
            <td class="confluenceTd">p2m {
             <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbspmodule('Login') {
             <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbspinclude(':lib-login')
             <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp}
             <br>}
            </td>
        </tr>
        <tr>
            <td class="confluenceTd"><img src="https://github.com/wangdaqi77/P2M/blob/master/assets/gradle_project.png" width="260"  alt="image"/></td>
            <td class="confluenceTd"><img src="https://github.com/wangdaqi77/P2M/blob/master/assets/p2m_module.png" width="260"  alt="image"/></td>
        </tr>
        </tbody>
    </table>
</div>

Module态
--------
一个Module态对应是一个模块，一个模块包含Api区和Source code区，Source code区可以访问自身和其所依赖模块的Api区。

<img src="https://github.com/wangdaqi77/P2M/blob/master/assets/p2m_module_detail.png" width="450"  alt="image"/><br/>

### Api区
Api区包含`launcher`、`service`、`event`，P2M注解处理器将参与编译。
 * `launcher` - 启动器，关联注解`@Launcher`，同一模块内可注解多个类，P2M注解处理器会生成相应的接口函数放入`launcher`中，目前支持注解Activity将生成`fun newActivityIntentOfXXActivity(): Intent`、注解Fragment将生成`fun newFragmentOfXXFragment(): Fragment`、注解Service将生成`fun newServiceIntentOfXXService(): Intent`；
 * `service`  - 服务，关联注解`@Service`，同一模块内只能注解一个类，P2M注解处理器会提取被注解类的所有公开成员函数放入`service`中，这样外部模块就可以间接调用到该模块的内部实现；
 * `event`    - 事件，关联注解`@Event`，同一模块内只能注解一个类，P2M注解处理器会提取被注解类中所有被`@EventField`注解的成员变量放入`event`，并根据变量的类型生成[可订阅的事件持有对象][live-event]（概况一下就是类似LiveData，但是比LiveData适合事件场景），用于发送事件和订阅接收事件，`@EventField`可以指定[可订阅的事件持有对象][live-event]发送事件和订阅接收事件是否占用主线程资源。

模块对外打开了一扇窗口，这扇窗就是Api区，找到窗口就找到了对应模块的launcher、service、event。

<img src="https://github.com/wangdaqi77/P2M/blob/master/assets/p2m_module_depend_on_module.png" width="450"  alt="image"/><br/>

#### Source code区如何访问Api区
当Api区需要更新时，我们必须先[编译Api区](#如何编译Api区)，这是访问Api区的前提。在模块内部能编写代码的区域都属于Source code区，我们在Source code区访问Api区：
```kotlin
val a = P2M.moduleApiOf<A>()          // 获取模块A的Api区

val launcherOfA = a.launcher       // Api区中的launcher
val serviceOfA = a.service         // Api区中的service
val eventOfA = a.event             // Api区中的event
```

### Source code区
Source code区是指模块内部可以编写和存放代码的区域，每个模块的Source code区是对外隐藏的，包含以下内容：
 * Module init      - 模块初始化，关联`@ModuleInitializer`注解，同一模块内只能注解一个类且必须实现ModuleInit接口，由P2M注解处理器生成代理类，每个模块必须声明此类，由开发者编码完成；
 * Implementation   - Api区的具体实现区，由P2M注解处理器生成，该部分开发者无需感知；
 * Feature code     - 编写功能代码区，由开发者编码完成。

模块需要开机后才可以提供给其他模块使用，开机就是在Module init区，它主要负责完成模块必要的初始化工作。

模块初始化工作有三个阶段：
 * `onEvaluate` - 评估自身阶段，主要用于注册完成自身初始化的任务，运行在后台线程。
 * `onExecute`  - 执行阶段，这里是执行注册的任务，运行在后台线程。
 * `onExecuted` - 完成执行阶段，注册的任务已经执行完毕，这里意味着模块已经完成初始化，运行在主线程。

模块初始化工作有以下定式：
 * 一个模块内，执行顺序一定为`onEvaluate` > `onExecute` > `onExecuted`。
 * 如果模块A依赖模块B，执行顺序一定为模块B的`onExecuted` > 模块A的`onExecute`。
 * 如果模块A依赖模块B，模块B依赖模块C，执行顺序一定为模块C的`onExecuted` > 模块A的`onExecute`。

了解以上你肯定有一些疑问，如Api区如何设计和使用、初始化阶段应该做些什么的等等，接下来我们从一个示例开始一一解惑。

示例
====
一个App，有三个界面分别为启动界面、登录界面和主界面:
 * 当启动应用时打开启动界面，在启动界面判断如果未登录过跳转到登录界面，否则跳转到主界面显示用户信息。
 * 当在登录界面登录成功后跳转到主界面显示用户信息。
 * 当在主界面点击退出登录跳转到登录界面。

需求分析
-------
经过分析需求，整个项目的文件树大致如下：
```
├── app                                     // app壳
│   ├── src
│   │   └── main/kotlin/package
│   │       ├── MyApp.kt                    // 自定义Application
│   │       └── SplashActivity.kt           // 启动界面
│   └── build.gradle
├── module-account                          // 模块Account
│   ├── src
│   │   ├── app/kotlin/package              // 模块作为app独立运行时的包
│   │   └── main/kotlin/package
│   │       ├── AccountModuleInit.kt        // 模块Account初始化类
│   │       ├── AccountEvent.kt             // 模块Account定义事件类
│   │       ├── AccountService.kt           // 模块Account服务类
│   │       ├── LoginUserInfo.kt            // 登录用户信息数据类
│   │       ├── UserDiskCache.kt            // 登录用户缓存管理类，负责读取缓存登录状态、登录信息
│   │       └── LoginActivity.kt            // 登录界面
│   └── build.gradle
├── module-main                             // 模块Main
│   ├── src
│   │   ├── app/kotlin/package              // 模块作为app独立运行时的包
│   │   └── main/kotlin/package
│   │       ├── MainModuleInit.kt           // 模块Main初始化类
│   │       └── MainActivity.kt             // 主界面
│   └── build.gradle
├── lib-http                                // http库
│   └── src
│       └── main/kotlin/package
│           └── Http.kt                     // Http请求类
├── build.gradle
└── settings.gradle                         // P2M项目配置
```

我们整个应用拆分为2个模块（存在2个Api区）+ 1个app壳：
 * 模块Account：
   * Api区主要负责对外提供登录状态（可订阅的事件持有对象）、登录信息（可订阅的事件持有对象）、登录成功（可订阅的事件持有对象）、登录界面Intent（启动器）、退登（服务）；
   * Source code区中的Module Init区负责声明AccountModuleInit（该模块的初始化），模块初始化时负责从本地缓存读取登录状态和登录用户信息，并分别将发送一个事件；
   * Source code区中的Feature code区主要负责登录界面UI和逻辑、实现登录和退登、读写登录相关缓存。

 * 模块Main：
   * Api区主要负责对外提供主界面Intent（启动器）；
   * Source code区中的Module Init区负责声明MainModuleInit（该模块的初始化），无任何初始化逻辑；
   * Source code区中的Feature code区主要负责主界面UI和逻辑。

 * app壳：
   * 负责打开P2M驱动（开始初始化所有模块）；
   * 订阅模块Account的登录状态，当登录成功则跳转模块Main的MainActivity（主界面），否则跳转模块Account的LoginActivity（登录界面）；
   * 在SplashActivity（启动界面）订阅模块Account的登录状态，如果是登录成功则跳转模块Main的MainActivity（主界面），否则跳转模块Account的LoginActivity（登录界面）

P2M项目配置
----------
位于根项目下的settings.gradle：
```groovy
// 务必添加buildscript，否则找不到相关插件
buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:4.0.2'            // AGP支持3.4.0+、Gradle 6.1.1+
        classpath 'com.github.wangdaqi77.P2M:p2m-plugin:last version'
    }
}

apply plugin: "p2m-android"                     // P2M插件

p2m {
    app {                                       // 声明app壳，可声明多个
        include(":app")                         // 声明project
        dependencies {                          // 声明依赖的模块，这里表示app壳依赖模块Account和模块Main
            module("Account")
            module("Main")
        }
    }

    module("Account") {                         // 声明模块Account
        include(":module-account")              // 声明project
    }

    module("Main") {                            // 声明模块Account
        include(":module-main")                 // 声明project
        dependencies {                          // 声明依赖的模块，这里表示模块Main依赖模块Account
            module("Account")       
        }  
    }
}

```

整个项目结构如下图所示：

<img src="https://github.com/wangdaqi77/P2M/blob/master/assets/p2m_project_example.png" width="400"  alt="image"/><br/>


模块Account
-----------
 * Api区是对外打开的一道门，因此我们先考虑如何设计Api区：

   * launcher - 对外提供登录界面的Intent，因此在Source code区定义：
        ```kotlin
        @Launcher
        class LoginActivity : Activity() // 具体实现请查看示例源码
        ```

        编译后将在Api区生成以下代码：
        ```kotlin
        /**
         * A launcher class of Account module.
         * Use `P2M.moduleApiOf<Account>().launcher` to get the instance.
         *
         * Use [newActivityIntentOfLoginActivity] to launch [com.p2m.example.account.pre_api.LoginActivity].
         */
        public interface AccountModuleLauncher : ModuleLauncher {
          public fun newActivityIntentOfLoginActivity(context: Context): Intent
        }
        ```

   * service - 对外提供退出登录的服务方法，因此在Source code区定义：
        ```kotlin
        @Service
        class AccountService { // 具体实现请查看示例源码
            fun logout() { }
        }
        ```
        编译后将在Api区生成以下代码：
        ```kotlin
        /**
         * A service class of Account module.
         * Use `P2M.moduleApiOf<Account>().service` to get the instance.
         *
         * @see com.p2m.example.account.pre_api.AccountService - origin.
         */
        public interface AccountModuleService : ModuleService {
          public fun logout(): Unit
        }
        ```

   * event - 对外提供登录状态，登录用户信息等，因此在Source code区定义：
        ```kotlin
        @Event
        interface AccountEvent{
            @EventField(eventOn = EventOn.MAIN, mutableFromExternal = false)        // 发送、订阅接收事件在主线程
            val loginInfo: LoginUserInfo?                                           // 登录用户信息

            @EventField                                                             // 发送、订阅接收事件在主线程（等效于@EventField(eventOn = EventOn.MAIN, mutableFromExternal = false)）
            val loginState: Boolean                                                 // 登录状态

            @EventField(eventOn = EventOn.BACKGROUND, mutableFromExternal = false)  // 发送、订阅接收事件不占用主线程资源
            val loginSuccess: Boolean                                               // 登录成功
            
            @EventField(eventOn = EventOn.MAIN, mutableFromExternal = true)         // mutableFromExternal = true，表示外部模块可以setValue和postValue，为了保证事件的安全性不推荐设置
            val testMutableEventFromExternal: Int                                   // 用于测试外部可变性
            
            val testAPT:Int     // 这个字段没有被注解，因此它将被过滤
        }
        ```
        编译后将在Api区生成以下代码：
        ```kotlin
        /**
         * A event class of Account module.
         * Use `P2M.moduleApiOf<Account>().event` to get the instance.
         *
         * @see com.p2m.example.account.pre_api.AccountEvent - origin.
         */
        public interface AccountModuleEvent : ModuleEvent {
          public val loginInfo: LiveEvent<LoginUserInfo?>
          public val loginState: LiveEvent<Boolean>
          public val loginSuccess: BackgroundLiveEvent<Unit>
          public val testMutableEventFromExternal: MutableLiveEvent<Int>
        }
        ```

 * Module init区是模块开机的地方，根据需求和模块的职责来设计所必须初始化工作。
    ```kotlin
    @ModuleInitializer
    class AccountModuleInit : ModuleInit {

        // 运行在子线程，用于注册模块内的任务，组织任务的依赖关系
        override fun onEvaluate(taskRegister: TaskRegister) {
            // 注册读取登录状态的任务
            taskRegister.register(LoadLoginStateTask::class.java, "input 1")

            // 注册读取登录用户信息的任务
            taskRegister
                .register(LoadLastUserTask::class.java, "input 2")
                .dependOn(LoadLoginStateTask::class.java) // 执行顺序一定为LoadLoginStateTask.onExecute() > LoadLastUserTask.onExecute()
        }

        // 运行在主线程，当所有的依赖模块完成开机且自身模块的任务执行完毕时调用
        override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
            val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java) // 获取登录状态
            val loginInfo = taskOutputProvider.getOutputOf(LoadLastUserTask::class.java)    // 获取用户信息

            val account = moduleProvider.moduleApiOf(Account::class.java)  // 找到自身的Api区，在Module init区不能调用P2M.moduleApiOf()
            account.event.mutable().loginState.setValue(loginState ?: false)      // 保存到事件持有者，提供给被依赖的模块使用
            account.event.mutable().loginInfo.setValue(loginInfo)                 // 保存到事件持有者，提供给被依赖的模块使用
        }
    }

    // 读取登录状态的任务，input:String output:Boolean
    class LoadLoginStateTask: Task<String, Boolean>() {

        // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
        override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
            val input = input // input的值是1
            Log.i("LoadLoginStateTask", "onExecute input: $input")

            val userDiskCache = UserDiskCache(moduleProvider.context)
            output = userDiskCache.readLoginState()
        }
    }

    // 注册读取登录用户信息的任务，input:String output:LoginUserInfo
    class LoadLastUserTask: Task<String, LoginUserInfo>() {

        // 运行在子线程，当所有的依赖模块完成开机且自身依赖的任务执行完毕时调用
        override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
            val loginState = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java)

            // 查询用户信息
            if (loginState == true) {
                val input = input // input的值是2
                Log.i("LoadLastUserTask", "onExecute input: $input")

                val userDiskCache = UserDiskCache(moduleProvider.context)
                output = userDiskCache.readLoginUserInfo()
            }
        }
    }
    ```

更多实现可以查看[所有的示例源代码][example]。

Q&A
===
如何将数据类发布到Api区？
----------------------
首先在数据类添加@ApiUse注解，最后[编译Api区](#如何编译Api区)。例如[示例中的LoginUserInfo][LoginUserInfo]

如何编译Api区？
-------------
如果Api区使用的相关注解（@Launcher、@Service、@Event、@EventField、@ApiUse）在代码中有增删改操作，需要点击Android Studio中的[Build][AS-Build] > Make Module或者[Build][AS-Build] > Make Project编译项目。

如何单独运行模块？
------------
主要需要配置开启运行app和applicationId等：
 1. 在声明模块代码块中增加`runApp = true`和`useRepo = false`，位于项目根目录下的settings.gradle：
    ```groovy
    p2m {
        module("YourModuleName") {
            // ...
            runApp = true               // true表示可以运行app
            useRepo = false             // false表示使用源码，true表示依赖仓库aar
        }
    }
    ```

 2. 在该模块文件夹下的build.gradle声明：
    ```groovy
    // 当该module设置`runApp=true`时才会应用这里的配置，必须放置文件的底部，以便覆盖以上已配置的值。
    p2mRunAppBuildGradle {
        android {
            defaultConfig{
                applicationId "your.application.package"
            }

            sourceSets {
                main {
                    java.srcDirs += 'src/app/java'                      // 在这里需要自定义Application，用于打开P2M驱动
                    manifest.srcFile 'src/app/AndroidManifest.xml'      // 在这里需要指定自定义的Application
                }
            }
        }
    }
    ```

 3. sync project

如何发布模块的aar等组件到仓库？
--------------------
发布前需要配置仓库等信息：
 1. 在声明模块代码块中增加以下配置，位于根项目下的settings.gradle：
    ```groovy
    p2m {
        module("YourModuleName") {
            // ...
            groupId = "your.repo.groupId"       // 组，主要用于确定发布的组，默认值模块名
            versionName = "0.0.1"               // 版本，主要用于确定发布的版本，默认值unspecified
            useRepo = false                     // false表示使用源码，true表示依赖仓库aar
        }

        p2mMavenRepository {                    // 声明maven仓库用于发布和获取远端aar, 默认为mavenLocal()
            url = "your maven repository url"   // 仓库地址
            credentials {                       // 登录仓库的用户
                username = "your user name"
                password = "your password"
            }
        }
    }
    ```

 2. 执行发布到仓库的命令
    * linux/mac下：
    ```shell
    ./gradlew publicYourModule                  // 用于发布单个模块
    ./gradlew publicAllModule                   // 用于发布所有的模块
    ```
    * windows下：
    ```shell
    gradlew.bat publicYourModule                // 用于发布单个模块
    gradlew.bat publicAllModule                 // 用于发布所有的模块
    ```

如何使用仓库aar？
---------------
前提是已经将模块打包到了仓库中，然后：
 1. 在声明模块代码块中增加以下配置，位于根项目下的settings.gradle：
    ```groovy
    p2m {
        module("YourModuleName") {
            // ...
            groupId = "your.repo.groupId"       // 组，主要用于确定发布的组，默认值模块名
            versionName = "0.0.1"               // 版本，主要用于确定发布的版本，默认值unspecified
            useRepo = true                      // false表示使用源码，true表示依赖仓库aar
        }

        p2mMavenRepository {                    // 声明maven仓库用于发布和获取远端aar, 默认为mavenLocal()
            url = "your maven repository url"   // 仓库地址
            credentials {                       // 登录仓库的用户
                username = "your user name"
                password = "your password"
            }
        }
    }
    ```
 2. sync project

混淆
====
因使用了[可订阅的事件持有对象][live-event]库，因此需要增加以下配置：
```
-dontwarn androidx.lifecycle.LiveData
-keep class androidx.lifecycle.LiveData { *; }
-dontwarn androidx.lifecycle.LifecycleRegistry
-keep class androidx.lifecycle.LifecycleRegistry { *; }
```

 [AS-Build]: https://developer.android.com/studio/run#reference
 [live-event]: https://github.com/wangdaqi77/live-event
 [example]: https://github.com/wangdaqi77/P2M/tree/master/example
 [LoginUserInfo]: https://github.com/wangdaqi77/P2M/blob/master/example/module-account/src/main/java/com/p2m/example/account/pre_api/LoginUserInfo.kt
