# Add project specific ProGuard rules here.

# ---- Android entry points (Activities, Application) ----
# R8 keeps these automatically via the manifest, but explicit rules are safer.
-keep public class com.veve.flowreader.FlowReader
-keep public class com.veve.flowreader.views.MainActivity
-keep public class com.veve.flowreader.views.BrowseFilesActivity
-keep public class com.veve.flowreader.views.PageActivity
-keep public class com.veve.flowreader.views.GetBookActivity

# ---- Room: database, entities, DAOs ----
# Room generates code at compile time that references these by name at runtime.
-keep class com.veve.flowreader.dao.AppDatabase { *; }
-keep class com.veve.flowreader.dao.BookRecord { *; }
-keep class com.veve.flowreader.dao.PageGlyphRecord { *; }
-keep class com.veve.flowreader.dao.ReportRecord { *; }
-keep class com.veve.flowreader.dao.Settings { *; }
-keep interface com.veve.flowreader.dao.DaoAccess { *; }

# ---- JNI: native methods ----
# The C++ side calls Java methods by their exact names; renaming breaks the bridge.
-keep class com.veve.flowreader.model.impl.djvu.DjvuBook {
    native <methods>;
}
-keep class com.veve.flowreader.model.impl.djvu.DjvuBookPage {
    native <methods>;
}
-keep class com.veve.flowreader.model.impl.pdf.PdfBook {
    native <methods>;
}
-keep class com.veve.flowreader.model.impl.pdf.PdfBookPage {
    native <methods>;
}

# ---- JNI: classes constructed by native code via FindClass/NewObject ----
# put_glyphs() in libnative-lib.so calls FindClass("com/veve/flowreader/model/PageGlyphInfo")
# and NewObject() to build glyph data — R8 must not remove or rename these.
-keep class com.veve.flowreader.model.PageGlyphInfo { *; }
