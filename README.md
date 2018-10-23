JKNF帮助文档![Release](https://jitpack.io/v/JackWHLiu/jknf.svg)  [![API](https://img.shields.io/badge/API-11%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=11)
================================
![avatar](http://jackwhliu.cn/static/images/banner3.jpg)

一、环境配置
--------------------------------
在gradle中配置环境。
#### //指定仓库的地址，在project的build.gradle加入粗体字的代码。
<blockquote>
allprojects {
  repositories {
    jcenter()
    <h3>maven { url "https://jitpack.io" }</h3>
  }
}
</blockquote>

#### //依赖本库，在app模块的build.gradle加入加粗的代码，版本号也可改成master-SNAPSHOT直接拿最新代码编译。
<blockquote>
dependencies {
    <h3>compile 'com.github.JackWHLiu.jknf:jknf-lib:1.0'</h3>
}
</blockquote>

二、如何使用(参考https://github.com/JackWHLiu/JackKnifeDemo)
--------------------------------
### (一)基于IOC依赖注入的自动注入视图、绑定控件和注册事件
#### 1、自动注入视图（Inject Layout）
##### （1）Activity继承org.jknf.app.Activity,Fragment继承org.jknf.app.Fragment
##### （2）保证布局的xml文件和Activity和Fragment的Java类的命名遵循一定的映射关系（Java类名必须以Activity或Fragment结尾）。
<blockquote>
    <b>前缀+名字，如activity_main</b>
    例如：MainActivity.java映射的xml文件名就为activity_main.xml，TTSFragment.java映射的xml文件名就为fragment_t_t_s.xml。
    Java文件以大写字母分隔单词，xml以下划线分隔单词。
</blockquote>
 
#### 2、自动绑定控件（Inject Views）
##### （1）不使用注解
> 直接在Activity或Fragment声明控件（View及其子类）为成员变量，不加任何注解。它会以这个View的名字来绑定该控件在xml中的id的value，即@+id/后指定的内容。
##### （2）使用@ViewInject
> 优先级比不加注解高，简单的说，加上这个注解就不会使用默认的使用成员属性名来对应xml的控件id的方式，而是使用该注解指定的id与xml的控件id绑定。
##### （3）使用@ViewIgnore
> 优先级最高，加上该注解，jackknife会直接跳过该控件的自动注入。一般使用在使用Java代码new出来的控件提取到全局的情况。也可以在ViewStub懒加载布局的时候使用。
#### 3、自动注册事件（Inject Events）
>  ）创建一个自定义的事件注解，在这个注解上配置@EventBase，并使用在你要实际回调的方法上，<b>注意保持参数列表跟原接口的某个回调方法的参数列表保持一致</b>。在jackknife-annotations-ioc中也提供了常用的事件的注解，比如@OnClick。

### (二)基于MVP设计理念的开发
#### 1、所需要依赖的类
##### （1）BaseModel（M层）
> 它是一个强大的数据筛选器，可以支持多条件筛选。
##### （2）IBaseView（V层）
> 在继承这个接口的接口中提供与界面显示相关的操作，比如显示某某数据，或获取从控件中获取用户输入的结果。建议继承这个
接口的接口也以I开头命名，避免与自定义View混淆。
##### （3）BasePresenter（P层）
> 在presenter中持有view和model的引用，它的职责是处理业务层的操作，如把model中的数据加载到view上显示、文件下载等。耗时操作务必在presenter中完成，jackknife-mvp可以有效防止activity的内存泄漏。
##### （4）BaseActivity或BaseFragment（V层）
> 比如public class MainActivity extends BaseActivity<IMainView, MainPresenter> implements
IMainView。你可以用jackknife提供的com.lwh.jackknife.mvp.BaseActivity，也可以参考它自己来实现。
#### 2、注意点
> 关于mvp这种架构，市面上众说纷纭，有支持的，也有不支持的。总之，mvp既有优点，也有缺点。先说优点，解除模型数据和UI显示的耦合，界面显示和业务操作逻辑分离，易于创建副本，提高可维护性。缺点也是显而易见的，Presenter和View类爆炸的问题很严重，也就是说，如果你只需要写一个很小的项目，是完全没有必要使用mvp的。当然，个人建议你在业务变化大的界面上使用mvp，而在一些简单的界面（如SplashActivity启动页）上没有必要使用。
