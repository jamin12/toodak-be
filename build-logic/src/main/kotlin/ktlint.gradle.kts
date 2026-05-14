plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

// 플러그인 적용 순서에 따라 prepareKotlinBuildScriptModel 태스크가 아직 없을 수 있으므로
// tasks.named() 대신 configureEach 로 지연 참조한다. (plugin-ktlint-lazy)
tasks.configureEach {
    if (name == "prepareKotlinBuildScriptModel") {
        dependsOn("addKtlintFormatGitPreCommitHook")
    }
}
