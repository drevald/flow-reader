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

# ---- JNI: classes accessed by native code via FindClass/GetMethodID ----
# put_glyphs() calls FindClass + NewObject for PageGlyphInfo.
# pdf-lib.cpp and djvu-lib.cpp call GetObjectClass(pageSize) + GetMethodID for setPageWidth/setPageHeight.
# R8 must not remove or rename any of these.
-keep class com.veve.flowreader.model.PageGlyphInfo { *; }
-keep class com.veve.flowreader.model.PageSize { *; }
