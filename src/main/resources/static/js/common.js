/**
 * Access Token을 localStorage에 저장하는 함수
 * @param {string} token - 저장할 JWT 토큰
 */
function saveTokenToLocalStorage(token) {
    try {
        localStorage.setItem('accessToken', token);
        console.log("토큰이 성공적으로 저장되었습니다.");
    } catch (error) {
        console.error("토큰 저장 중 오류 발생:", error);
    }
}

/**
 * 로컬스토리지에서 Access Token을 가져오는 함수
 * @returns {string|null} - 저장된 JWT 토큰 또는 null
 */
function getTokenFromLocalStorage() {
    try {
        const token = localStorage.getItem('accessToken');

        if (token) {
            console.log("토큰을 성공적으로 가져왔습니다.");
            return token;
        } else {
            console.warn("로컬스토리지에 저장된 토큰이 없습니다.");
            return null;
        }

    } catch (error) {
        console.error("토큰 가져오는 중 오류 발생:", error);
        return null;
    }
}

// 로컬스토리지에서 토큰 삭제 함수
function removeTokenFromLocalStorage() {
    try {
        localStorage.removeItem('accessToken');
        console.log("토큰이 성공적으로 삭제되었습니다.");
    } catch (error) {
        console.error("토큰 삭제 중 오류 발생:", error);
    }
}

// 로그아웃 버튼 이벤트 리스너 추가
document.getElementById('logoutBtn').addEventListener('click', function () {
    // 서버에 로그아웃 요청 (토큰 없이)
    axios.post('/auth/logout', {}, {
        headers: {
            'Content-Type': 'application/json'
        },
        withCredentials: true  // 쿠키와 같은 자격 증명 포함 (필요한 경우)
    })
        .then(response => {
            console.log("로그아웃 성공:", response.data);
            removeTokenFromLocalStorage();  // 로컬스토리지에서 토큰 삭제
            window.location.href = '/';     // 루트 페이지로 리다이렉트
        })
        .catch(error => {
            console.error("로그아웃 실패:", error);
            alert("로그아웃 중 오류가 발생했습니다. 다시 시도해주세요.");
        });
});

// 에러 메시지를 필드별로 표시하는 함수
function handleErrors(errors) {
    for (const field in errors) {
        const errorElement = document.getElementById(`${field}Error`);
        if (errorElement) {
            errorElement.innerText = errors[field];
        }
    }
}

// 기존 에러 메시지 초기화 함수
function clearErrors() {
    const errorMessages = document.querySelectorAll(".error-message");
    errorMessages.forEach(element => element.innerText = "");
}

// 메시지 초기화 함수 (form 전체 초기화 포함)
function clearMessage() {
    // 1. 상단 메시지 초기화
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = '';       // 상단 메시지 텍스트 초기화
    messageDiv.className = 'message';  // 클래스 초기화 (스타일 리셋)

    // 2. 각 필드의 에러 메시지 초기화 (span.error-message 초기화)
    const errorMessages = document.querySelectorAll('.error-message');
    errorMessages.forEach(span => span.textContent = '');

    // 3. 입력 필드 테두리 스타일 초기화 (선택사항)
    const inputFields = document.querySelectorAll('input');
    inputFields.forEach(input => input.classList.remove('error-border'));  // 스타일 초기화 (선택 시)
}

fetchUserInfo();

// 로그인한 사용자 정보 가져오기
function fetchUserInfo() {
    const token = getTokenFromLocalStorage();

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

function enableAuthControls(...elementIds) {
    elementIds.forEach(elementId => {
        const element = document.getElementById(elementId);
        if (element) {
            element.disabled = false; // 버튼 활성화
        }
    });
}

function disableAuthControls(...elementIds) {
    elementIds.forEach(elementId => {
        const element = document.getElementById(elementId);
        if (element) {
            element.disabled = true; // 버튼 비활성화
        }
    });
}

// 페이지 자동 새로고침
setInterval(fetchUserInfo, 5000);