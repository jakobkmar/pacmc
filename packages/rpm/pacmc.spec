Name:     pacmc
Version:  ${version}
Release:  1%{?dist}
Summary:  ${description}
License:  AGPLv3+
URL:      ${githubUrl}
Source0:  ${githubUrl}/releases/download/%{version}/pacmc-%{version}.tar

%description
${description}

%prep
%autosetup

%build

%install
mkdir -p %{buildroot}/usr/share/%{name}/
cp -r . %{buildroot}/usr/share/%{name}/

mkdir -p %{buildroot}/usr/bin/
ln -sf /usr/share/%{name}/bin/%{name} %{buildroot}/usr/bin/%{name}

%files
/usr/bin/%{name}
%dir /usr/share/%{name}/

