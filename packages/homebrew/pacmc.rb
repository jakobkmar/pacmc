class Pacmc < Formula
    desc "An easy-to-use Minecraft package manager and launcher"
    homepage "https://github.com/jakobkmar/pacmc"
    url "https://github.com/jakobkmar/pacmc/releases/download/0.5.0/pacmc-0.5.0.tar"
    sha256 "f90b5773a125e99ca10423be615f805337d394561cd25bbbf4d80f37aa3459fe"
    license "AGPL-3.0-or-later"
  
    depends_on "openjdk"
    
    def install
      rm_f Dir["bin/*.bat"]
      libexec.install %w[bin lib]
      env = Language::Java.overridable_java_home_env
      (bin/"pacmc").write_env_script libexec/"bin/pacmc", env
    end
  end