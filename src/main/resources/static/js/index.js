fetchUserInfo();

// 로그인한 사용자 정보 가져오기
function fetchUserInfo() {
    const token = getTokenFromLocalStorage();

    // if (!token) {
    //     alert("로그인이 필요합니다.");
    //     window.location.href = '/login';
    //     return;
    // }

    axios.get('/api/me', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        withCredentials: true // 쿠키와 같은 자격 증명 포함
    })
        .then(response => {
            if (response.data.status) {
                displayUserInfo(response.data.data);  // 사용자 정보를 화면에 표시하는 함수
            }
        })
        .catch(error => {
            if (error.response && error.response.status !== 1) {
                // alert("인증이 만료되었습니다. 다시 로그인 해주세요.");
                // window.location.href = '/login';
                console.log("회원 정보를 가져오는 데 실패했습니다.");
                // 토큰 만료
                if (error.response.data.status === "GONE") {
                    requestTokenReissue();
                }
            }
        });
}

// 로컬 스토리지에서 토큰 가져오는 함수
function getTokenFromLocalStorage() {
    return localStorage.getItem('accessToken') || "토큰 없음";
}

// 토큰 정보를 화면에 표시하는 함수
function displayStoredToken() {
    const storedTokenElement = document.getElementById('storedToken');
    const token = getTokenFromLocalStorage();
    storedTokenElement.textContent = token;
}

// 페이지 로드 후 토큰 표시
document.addEventListener('DOMContentLoaded', displayStoredToken);

// 사용자 정보를 화면에 표시하는 함수
function displayUserInfo(userInfo) {
    // 로그인 및 회원가입 링크 숨기기
    document.getElementById('loginLink').style.display = 'none';
    document.getElementById('signupLink').style.display = 'none';

    // 사용자 이메일 표시
    const userEmailElement = document.getElementById('userEmail');
    userEmailElement.textContent = `${userInfo.email} 님 환영합니다!`;
    userEmailElement.style.display = 'inline';  // 이메일 표시

    // 로그아웃 버튼 표시
    document.getElementById('logoutBtn').style.display = 'inline';

    document.getElementById('generalUser').style.display = 'none';
    document.getElementById('memberUser').style.display = 'none';
    document.getElementById('adminUser').style.display = 'none';

    if (userInfo.role === 'ADMIN') {
        document.getElementById('adminUser').style.display = 'block';
    } else if (userInfo.role === 'USER') {
        document.getElementById('memberUser').style.display = 'block';
    } else {
        document.getElementById('generalUser').style.display = 'block';
    }
}

// 토큰 재발급 요청 함수
function requestTokenReissue() {
    axios.post('/reissue', {}, {
        headers: {
            'Content-Type': 'application/json'
        },
        withCredentials: true  // 쿠키 포함 요청
    })
        .then(response => {
            if (response.status === 200) {
                console.log("토큰 재발급 성공:", response.data);

                // Authorization 헤더에서 새로운 Access Token 추출
                const authHeader = response.headers.authorization;
                if (authHeader && authHeader.startsWith('Bearer ')) {
                    const newAccessToken = authHeader.split(' ')[1];

                    // 새로운 Access Token을 로컬스토리지에 저장
                    saveTokenToLocalStorage(newAccessToken);
                    fetchUserInfo();
                    console.log("토큰이 성공적으로 재발급되었습니다.");

                    // 페이지 자동 새로고침
                    setTimeout(() => {
                        window.location.reload();
                    }, 200); // 0.2초 후 새로고침 (사용자가 변화를 확인할 시간 확보)
                } else {
                    console.error("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                }
            }
        })
        .catch(error => {
            console.error("토큰 재발급 실패:", error.response.data.message);
            alert(error.response.data.message)
            // alert("토큰 재발급 중 오류가 발생했습니다. 다시 시도해주세요.");
        });
}

// 예제: 버튼 클릭 시 토큰 재발급 요청
document.getElementById('reissueTokenBtn').addEventListener('click', requestTokenReissue);

// 페이지 자동 새로고침
setInterval(fetchUserInfo, 5000);