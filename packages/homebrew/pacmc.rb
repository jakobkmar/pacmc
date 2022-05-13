class Pacmc < Formula
  desc "Minecraft package manager and launcher"
  homepage "${githubUrl}"
  url "${githubUrl}/releases/download/${version}/pacmc-${version}.tar"
  sha256 "${tarHashSha256}"
  license "${license}"

  depends_on "openjdk"

  def install
    rm_f Dir["bin/*.bat"]
    libexec.install %w[bin lib]
    env = Language::Java.overridable_java_home_env
    (bin/"pacmc").write_env_script libexec/"bin/pacmc", env
  end

  test do
    assert_match(/Hello user! pacmc works./, shell_output("#{bin}/pacmc debug test"))
  end
end