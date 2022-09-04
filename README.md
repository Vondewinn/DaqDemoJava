# DaqDemoJava
Java

操作步骤：

1、在项目下的build.gradle中加入maven { url 'https://jitpack.io' }
如：
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2、在app的build.gradle中添加依赖
如：
dependencies {
    ...
    implementation 'com.github.Vondewinn:canX:0.1-alpha7'
    implementation 'com.github.Vondewinn:uartsdk:0.1-alpha07'
    ...
}
3、点击同步到项目中，这样就完成了调用第三方依赖包


