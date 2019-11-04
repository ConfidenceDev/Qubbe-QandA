# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class <live.qubbe.android.App>

-keep public class <live.qubbe.android.activities.ContributorsActivity>
-keep public class <live.qubbe.android.activities.ResponseActivity>
-keep public class <live.qubbe.android.activities.EditAccountActivity>
-keep public class <live.qubbe.android.activities.LicenseActivity>
-keep public class <live.qubbe.android.activities.MainActivity>
-keep public class <live.qubbe.android.activities.PayActivity>
-keep public class <live.qubbe.android.activities.SettingsActivity>
-keep public class <live.qubbe.android.activities.SignInActivity>
-keep public class <live.qubbe.android.activities.TermsActivity>

-keep public class <live.qubbe.android.adapters.CardAdapter>
-keep public class <live.qubbe.android.adapters.ContributorsAdapter>
-keep public class <live.qubbe.android.adapters.ResponseAdapter>
-keep public class <live.qubbe.android.adapters.MyPostsAdapter>

-keep public class <live.qubbe.android.fragments.ResponseFragment>
-keep public class <live.qubbe.android.fragments.MainFragment>
-keep public class <live.qubbe.android.fragments.MyPostsFragment>
-keep public class <live.qubbe.android.fragments.NewFragment>

-keep public class <live.qubbe.android.models.CardModel>
-keep public class <live.qubbe.android.models.CotributorsModel>
-keep public class <live.qubbe.android.models.ResponseModel>
-keep public class <live.qubbe.android.models.MyPostsModel>
