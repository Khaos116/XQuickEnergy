# 保留整个项目的所有类和接口
-keep class io.github.lazyimmortal.sesame.** { *; }
# 保留 TypeReference 及其子类
-keep class com.fasterxml.jackson.core.type.TypeReference { *; }
-keep class * extends com.fasterxml.jackson.core.type.TypeReference { *; }

# 保留 JSON 反序列化所需的字段
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
}

-obfuscationdictionary proguard-sxbk.txt
-classobfuscationdictionary proguard-sxbk.txt
-packageobfuscationdictionary proguard-sxbk.txt