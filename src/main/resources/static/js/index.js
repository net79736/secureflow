// 토큰 정보를 화면에 표시하는 함수
function displayStoredToken() {
    const storedTokenElement = document.getElementById('storedToken');
    storedTokenElement.textContent = getTokenFromLocalStorage();
}

// 페이지 로드 후 토큰 표시
document.addEventListener('DOMContentLoaded', displayStoredToken);

// 예제: 버튼 클릭 시 토큰 재발급 요청
document.getElementById('reissueTokenBtn').addEventListener('click', requestTokenReissue);

// 사용자 정보를 화면에 표시하는 함수
function displayUserInfo(userInfo) {
    const {email, role} = userInfo;

    // 로그인 및 회원가입 링크 숨기기
    document.getElementById('loginLink').style.display = 'none';
    document.getElementById('signupLink').style.display = 'none';

    // 사용자 이메일 표시
    const userEmailElement = document.getElementById('userEmail');
    userEmailElement.textContent = `${email} 님 환영합니다!`;
    userEmailElement.style.display = 'inline';  // 이메일 표시

    // 로그아웃 버튼 표시
    document.getElementById('logoutBtn').style.display = 'inline';

    document.getElementById('generalUser').style.display = 'none';
    document.getElementById('memberUser').style.display = 'none';
    document.getElementById('adminUser').style.display = 'none';

    if (role === 'ADMIN') {
        document.getElementById('adminUser').style.display = 'block';
    } else if (role === 'USER') {
        document.getElementById('memberUser').style.display = 'block';
    } else {
        document.getElementById('generalUser').style.display = 'block';
    }
}