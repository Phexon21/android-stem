# Android String XML Reference

What is it
---
Android String XML Reference is a Gradle plugin which will
resolve placeholders of XML strings referenced in other XML strings
at buildtime. You won't have to write any Java/Kotlin code into your
project to make it work and you will still be able to access to the 'resolved'
strings the same way as any other manually added string to your
XML files.

In other words, if you're looking to do something like this:

#### Input:
```xml
<resources>
    <string name="app_name">My App Name</string>
    <string name="welcome_message">Welcome to ${app_name}</string>
</resources>
```
#### Output:
```xml
<resources>
    <string name="app_name">My App Name</string>
    <string name="welcome_message">Welcome to My App Name</string>
</resources>
```
Without having to write any Java/Kotlin code, then this plugin might help you.

How to use
---
### 1.- Defining your templates
The first thing you need to do is to define your string templates,
the ones that will contain references to other strings, you can do so
in any of your project's XML values files. Based on the example above,
we'll need a template for the string `welcome_message`:
```xml
<resources>
    <string name="app_name">My App Name</string>
    <string name="template_welcome_message">Welcome to ${app_name}</string>
</resources>
```
Every template must start with the `template_` prefix (this prefix will be
removed for the resolved string). Also, every placeholder must have
this format: `${ANOTHER_STRING_NAME}`.

A template can contain from zero to any amount of placeholders. Any string within your values folder (even other templates) can be referenced
inside a placeholder (just please be careful of not creating circular
dependencies between templates).

### 2.- Build
By default, the task that resolves the string templates will run during
your app's build process, unless you want to change this and rather
running it manually, which is explained below under `Running it manually`.

After the task finishes, you will be able to see a new XML file
in your app's values folder, named `resolved.xml`, which will contain
all of your string templates resolved. For our example, it should look
like this:
```xml
<!--resolved.xml-->
<resources>
    <string name="welcome_message">Welcome to My App Name</string>
</resources>
```

And that's it, you can now keep on working on important things without
caring of writing any code to resolve these placeholders later on
and/or having to write the same values for many different
string resources.

The following cases are supported:

- Regular base strings.
- Multi-language strings.
- Flavor specific strings.
- Flavor specific with multi-language strings.

Adding it to your project
---
In order to add it to your project, first you'll need to add this
classpath into your `buildscript` dependencies block of your root project
`build.gradle` file:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        //...
        classpath "com.likethesalad.android:string-reference:1.0.0"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
```

And then in your application's `build.gradle` file:

```groovy
apply plugin: 'com.android.application'
apply plugin: 'placeholder-resolver'

android {
  //...
}
```

Running it manually
---
The task that resolves the string templates is incremental,
it won't run unless there are changes to the templates and/or
their values. Meaning that your build process won't have to run this
task every time you compile, but only when it is needed.

If you still want to avoid running the templates resolver task
automatically during the build process and rather running it manually, then
you can turn it off by adding the following into your application's
`build.gradle` file, where you've added this plugin:
```groovy
stringXmlReference {
  resolveOnBuild = false
}
```

And that's it, now you'll have to make sure to trigger the task
manually whenever you need your resolved strings to get
updated by running: `resolve[BUILD_VARIANT]Placeholders` depending
on your build configuration. For example, to run it for the debug variant,
you'll have to run: `resolveDebugPlaceholders`, or if you have flavors
setup in your application, e.g. say you have 'demo' as a flavor defined,
then you can run `resolveDemoDebugPlaceholders` to generate the strings
for the demo flavor on the debug variant and so on.

License
---
    MIT License
    
    Copyright (c) 2019 LikeTheSalad.
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.