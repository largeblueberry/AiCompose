package com.largeblueberry.core_ui

import androidx.compose.ui.graphics.Color

object LightColors {

    // 앱 전체에서 사용되는 공통 배경 색상
    val AppBackground = Color(0xFFF7FAFC)
    val AppWhite = Color(0xFFFFFFFF) // 기존 White
    val AppBlack = Color(0xFF000000) // 기존 Black
    val AppRed = Color(0xFFFF0000)
    val AppPrimaryBlue = Color(0xFF4F8CFF)

    // 제목용 파란색 팔레트 (이어름)
    val TitleBlueDark = Color(0xFF2362EB)    // 진한 파란색
    // 부제목용 연한 파란색 팔레트 (듣고, 꿈꾸는 AI 작곡 서비스)
    val SubtitleBlue = Color(0xFF7987F8)    // 연한 보라빛 파란색

    // 텍스트 색상
    val AppTextDark = Color(0xFF222222) // 기존 DarkTextColor
    val CardViewMainText = Color(0xFF2563EB) // 카드뷰 메인 제목 글자 색
    val CardViewSubText = Color(0xFF7B93B8) // 카드뷰 서브 제목 글자 색

    // 특정 목적의 색상
    val RecordingRed = Color(0xFFFF4F4F) // 녹음 중일 때 버튼 색상
    val ProgressBarBackgroundTint = Color(0xFFE0E7EF) // 기존 ProgressBackgroundTint
    val CardViewBackGround = Color(0xFF3B82F6) // 카드뷰 배경 색

    // setting screen colors
    val SettingUtilColor = Color(0xFF999999)  // '또는', 버전 글자 색상도 동일
    val SettingItemMainText = Color(0xFF333333)
    val UtilTextColor = Color(0xFF666666) //setting, login 쪽 글자 색상
    val SettingBackground = Color(0xFFF8F9FA) // 섹션 배경 색상
    val SettingBasicUser = Color(0xFF4285F4)

    // login screen colors
    val googleDisabledContainerColor = Color(0xFFE0E0E0) // 비활성화 시 연한 회색 배경
    val googleDisabledContentColor = Color(0xFF9E9E9E)  // 비활성화 시 연한 회색 글자
    val googleButtonBorderColor = Color(0xFFDADCE0) // 구글 로그인 테두리 : 연한 회색 테두리

    val utilHorizontalDivider = Color(0xFFE0E0E0) // 수평 구분선: 연한 회색

}