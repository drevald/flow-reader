# Keep all test classes — R8 must not strip test framework or our test code.
-keep class * { *; }
-keepattributes *
-dontwarn **
