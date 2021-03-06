import android.Keys._

platformTarget in Android := Common.androidPlatformTarget

minSdkVersion in Android := Common.androidMinSdkVersion

targetSdkVersion in Android := Common.androidTargetSdkVersion

name := """goseumdochi-watchdog"""

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

// FIXME:  add back in "-deprecation", "-Xfatal-warnings"
scalacOptions ++= Common.scalacOptionsAllowWarnings

autoCompilerPlugins := true

classpathTypes += "maven-plugin"

resolvers ++= Common.resolvers

libraryDependencies ++= Common.ffmpegPlatformDeps("compile", "android-arm")

// Override the run task with the android:run
run <<= run in Android

useProguard in Android := true

useProguardInDebug in Android := true

proguardScala in Android := true

dexMulti in Android := true

dexMaxProcessCount := 1

dexMainClasses in Android := Common.dexMainClasses

proguardOptions in Android ++= Common.proguardOptions

packagingOptions in Android := PackagingOptions(merges=Seq("reference.conf"), excludes=Common.packagingExcludes)

javacOptions ++= Seq("-target", "1.7", "-source", "1.7")

maxErrors := Common.maxErrors

traceLevel := Common.traceLevel
