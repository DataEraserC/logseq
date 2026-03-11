{
  pkgs,
  lib,
  config,
  inputs,
  ...
}:

{
  # https://devenv.sh/basics/
  env = {
    # Java version for Android builds
    JAVA_HOME = "${pkgs.jdk21}";

    # Node.js version requirement (package.json: >=22.20.0)
    NODE_ENV = "development";
  };

  # https://devenv.sh/packages/
  packages = with pkgs; [
    # Core build tools
    git
    curl
    wget

    # Java - JDK 21 for Android builds
    jdk21

    # Node.js 22.x
    nodejs_22

    # Yarn package manager
    yarn

    # Clojure CLI
    clojure
    clojure-lsp

    # System libraries required for native modules
    # From Dockerfile: libcairo2-dev libpango1.0-dev libjpeg-dev libgif-dev librsvg2-dev
    cairo
    pango
    libjpeg
    giflib
    librsvg
    pkg-config
    gcc

    # Required by better-sqlite3 (native Node module)
    node-gyp

    # Required by node-canvas
    pixman
    libpng
  ];

  # https://devenv.sh/scripts/
  scripts.build-deps.exec = ''
    echo "Installing dependencies..."
    yarn install --frozen-lockfile --ignore-scripts || yarn install
  '';

  # https://devenv.sh/basics/
  enterShell = ''
    # Display environment info
    java -version
    node --version
    clojure -M -e "(println (clojure-version))" || echo "Clojure CLI ready"
    yarn --version || echo "Yarn ready"
    echo "JAVA_HOME: $JAVA_HOME"
  '';

  # https://devenv.sh/tasks/
  tasks = {
    # Android build task (requires manual Android SDK setup)
    # Run: sdkmanager --licenses to accept Android SDK licenses
    "android:build".exec = ''
      echo "Building Android..."
      echo "Please ensure Android SDK is installed and licenses accepted"
      echo "Run: sdkmanager --licenses"
      yarn install && yarn release-mobile
      cd android && ./gradlew assembleDebug
    '';

    "android:release".exec = ''
      echo "Building Android release..."
      yarn install && yarn release-mobile
      cd android && ./gradlew zipApksForRelease
    '';

    # iOS build task (placeholder - requires macOS)
    "ios:build".exec = ''
      echo "iOS build is only supported on macOS"
      exit 1
    '';

    # Desktop build task (Electron)
    "desktop:build".exec = ''
      echo "Building Desktop..."
      yarn install && yarn release
    '';

    "desktop:dev".exec = ''
      echo "Starting Desktop development..."
      yarn dev
    '';

    # Mobile dev task
    "mobile:dev".exec = ''
      echo "Starting Mobile development..."
      yarn mobile-watch
    '';
  };

  # https://devenv.sh/tests/
  enterTest = ''
    java -version 2>&1 | grep -q "version \"21" || echo "Java 21 not found"
    node --version | grep -q "^v22" || echo "Node.js 22 not found"
    yarn --version || echo "Yarn not found"
    echo "Development environment ready"
  '';
}
