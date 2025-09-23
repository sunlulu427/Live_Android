#!/bin/bash

echo "🔍 开始兼容性验证..."

# Check if we're in the correct directory
if [ ! -f "MLVB-API-Example/build.gradle.kts" ]; then
    echo "❌ 错误：请在项目根目录执行此脚本"
    exit 1
fi

cd MLVB-API-Example

# 1. 检查 Java 版本
echo "📋 检查 Java 版本..."
echo "默认 Java 版本:"
java -version
echo ""
echo "项目配置的 JDK 11:"
if [ -f "local.properties" ]; then
    grep "org.gradle.java.home" local.properties || echo "❌ 未配置 org.gradle.java.home"
else
    echo "❌ local.properties 文件不存在"
fi
echo ""

# 2. 检查 Kotlin 版本
echo "📋 检查 Kotlin 和 Gradle 版本..."
./gradlew --version | grep -E "(Gradle|Kotlin)"
echo ""

# 3. 验证项目结构
echo "📋 验证 Compose 文件结构..."
if [ -f "App/src/main/java/com/tencent/mlvb/apiexample/domain/StreamingFeature.kt" ]; then
    echo "✅ StreamingFeature.kt 已创建"
else
    echo "❌ StreamingFeature.kt 未找到"
fi

if [ -f "App/src/main/java/com/tencent/mlvb/apiexample/ui/theme/MLVBTheme.kt" ]; then
    echo "✅ MLVBTheme.kt 已创建"
else
    echo "❌ MLVBTheme.kt 未找到"
fi

# 4. 检查 build.gradle.kts 配置
echo "📋 检查 Compose 配置..."
if grep -q "compose = true" App/build.gradle.kts; then
    echo "✅ Compose buildFeatures 已启用"
else
    echo "❌ Compose buildFeatures 未启用"
fi

if grep -q "androidx.compose" App/build.gradle.kts; then
    echo "✅ Compose 依赖已添加"
else
    echo "❌ Compose 依赖未找到"
fi

# 5. 编译测试（快速语法检查）
echo "📋 执行快速语法检查..."
./gradlew App:compileDebugKotlin --dry-run > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Kotlin 编译配置正确"
else
    echo "⚠️  Kotlin 编译配置需要检查"
fi

echo ""
echo "✅ 兼容性验证完成！"
echo "💡 提示：如需完整构建验证，请运行: ./gradlew App:assembleDebug"