package com.largeblueberry.setting.about

/**
 * 앱(APK)에 함께 배포되는 오픈소스와 그 고지. 이름·저작권·라이선스 이름은 고유명사라 번역하지 않는다.
 * 원문은 이 모듈의 assets/licenses에 둔다(다른 모듈의 assets 경로에 기대지 않는다).
 *
 * 목록은 손으로 관리한다. 의존성을 더하거나 OSMD·모델을 바꾸면 여기도 고친다(docs/troubleshooting.md C-11).
 * OSMD 번들 안의 라이브러리는 OSMD 3.0.0의 package.json과 번들 코드에서 확인했다.
 */
internal data class OpenSourceLicense(
    val name: String,
    /** 화면에 그대로 보이는 저작권 문구 */
    val copyright: String,
    val licenseName: String,
    /** assets 안의 라이선스 원문 경로 */
    val licenseAsset: String,
    /** Apache-2.0의 NOTICE처럼 함께 보여야 하는 고지(있으면) */
    val noticeAsset: String? = null,
)

internal object OpenSourceLicenses {
    const val APACHE_2_0 = "licenses/apache-2.0.txt"
    const val MIT = "licenses/mit.txt"
    const val OSMD_BSD_3 = "licenses/osmd-bsd-3-clause.txt"
    const val BASIC_PITCH_NOTICE = "licenses/basic-pitch-notice.txt"

    private const val APACHE = "Apache License 2.0"
    private const val MIT_NAME = "MIT License"

    val all: List<OpenSourceLicense> = listOf(
        // ── 기기 안 채보 ──
        OpenSourceLicense("Basic Pitch (model)", "Copyright 2022 Spotify AB", APACHE, APACHE_2_0, BASIC_PITCH_NOTICE),
        OpenSourceLicense("LiteRT", "Copyright Google LLC", APACHE, APACHE_2_0),

        // ── 악보 그리기(앱 assets의 OSMD 번들) ──
        OpenSourceLicense("OpenSheetMusicDisplay 3.0.0", "Copyright 2019 PhonicScore", "BSD 3-Clause License", OSMD_BSD_3),
        OpenSourceLicense("VexFlow 1.2.93", "Copyright (c) 2010 Mohit Muthanna Cheppudira", MIT_NAME, MIT),
        // MIT 또는 GPLv3 중 선택하는 이중 라이선스. MIT로 쓴다.
        OpenSourceLicense(
            "JSZip 3.10.2",
            "Copyright (c) 2009-2016 Stuart Knightley, David Duponchel, Franz Buchinger, António Afonso",
            "$MIT_NAME (dual-licensed MIT or GPLv3; used under MIT)",
            MIT,
        ),
        OpenSourceLicense("pako", "Copyright (C) 2014-2017 by Vitaly Puzrin and Andrei Tuputcyn", MIT_NAME, MIT),
        OpenSourceLicense("lie", "Copyright (c) 2014-2018 Calvin Metcalf, Jordan Harband", MIT_NAME, MIT),
        OpenSourceLicense(
            "immediate",
            "Copyright (c) 2012 Barnesandnoble.com, llc, Donavon West, Domenic Denicola, Brian Cavalier",
            MIT_NAME,
            MIT,
        ),
        OpenSourceLicense(
            "setImmediate",
            "Copyright (c) 2012 Barnesandnoble.com, llc, Donavon West, and Domenic Denicola",
            MIT_NAME,
            MIT,
        ),
        OpenSourceLicense("typescript-collections 1.3.3", "Copyright (c) 2010-2017 Tomasz Ciborski", MIT_NAME, MIT),
        OpenSourceLicense("loglevel", "Copyright (c) 2013 Tim Perry", MIT_NAME, MIT),

        // ── Android ──
        OpenSourceLicense(
            "AndroidX (Jetpack Compose, Room, WorkManager, DataStore, WebKit, …)",
            "Copyright The Android Open Source Project",
            APACHE,
            APACHE_2_0,
        ),
        OpenSourceLicense(
            "Kotlin, kotlinx.coroutines",
            "Copyright JetBrains s.r.o. and Kotlin Programming Language contributors",
            APACHE,
            APACHE_2_0,
        ),
        OpenSourceLicense("Dagger, Hilt", "Copyright The Dagger Authors", APACHE, APACHE_2_0),
    )
}
