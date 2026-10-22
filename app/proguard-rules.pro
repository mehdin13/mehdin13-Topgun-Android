# kotlinx.serialization keeps its generated serializers via the plugin; OkHttp ships its own consumer rules.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
