let verifiedEmail = null; // 인증이 완료된 이메일을 저장하는 변수

document.getElementById('emailVerifyBtn').addEventListener('click', function () {
    clearMessage();  // 메시지 초기화
    const email = document.getElementById('email').value;

    axios.post('/api/verification/send', {
        email: email
    }, {
        headers: {
            'Content-Type': 'application/json'
        },
        withCredentials: true
    })
        .then(response => {
            if (response.data.status) {
                alert("이메일을 전송하였습니다.")
                verifiedEmail = null;
                enableAuthControls("codeVerifyBtn");
            }
        })
        .catch(error => {
            handleErrors(error.response.data.data);
        });
});

document.getElementById("signupForm").addEventListener("submit", function (event) {
    event.preventDefault(); // 폼의 기본 제출 막기 (페이지 새로고침 방지)

    if (!validateVerifiedEmail()) {
        document.getElementById("verificationCodeError").innerHTML = "인증되지 않은 이메일 입니다.";
        return;
    }

    // 기존 에러 메시지 초기화
    clearErrors();

    // 폼 데이터 수집
    const formData = {
        email: document.getElementById("email").value,
        // verificationCode: document.getElementById("verificationCode").value,
        password: document.getElementById("password").value,
        confirmPassword: document.getElementById("confirmPassword").value,
        nickname: document.getElementById("nickname").value
    };

    // 서버로 데이터 전송 (Axios 요청)
    axios.post("/api/me", formData, {
        headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (response.data.status) {
                // Authorization 헤더에서 Bearer 토큰 추출
                const authHeader = response.headers.authorization;

                if (authHeader && authHeader.startsWith('Bearer ')) {
                    const token = authHeader.split(' ')[1];  // 'Bearer ' 이후의 토큰 부분 추출

                    // localStorage 에 토큰 저장
                    saveTokenToLocalStorage(token);

                    alert("회원가입이 완료되었습니다.");

                    location.href = "/";
                } else {
                    console.error("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                }
            }
        })
        .catch(error => {
            if (error.response.data.data) {
                handleErrors(error.response.data.data);
            } else {
                alert(error.response.data.message);
            }
        });
});

document.getElementById('codeVerifyBtn').addEventListener('click', function () {
    clearMessage();  // 메시지 초기화
    const email = document.getElementById('email').value;
    const code = document.getElementById('verificationCode').value;

    axios.post('/api/verification/verify-code', {
        email: email,
        code: code
    }, {
        headers: {
            'Content-Type': 'application/json'
        },
        withCredentials: true
    })
        .then(response => {
            if (response.data.status) {
                alert("입력된 인증코드가 확인되었습니다.");
                verifiedEmail = document.getElementById("email").value;
            }
        })
        .catch(error => {
            console.error(error);
            if (error.response.data.data) {
                handleErrors(error.response.data.data);
            } else {
                alert(error.response.data.message);
            }
        });
});

document.getElementById("checkDuplicateBtn").addEventListener("click", function () {
    clearMessage();  // 메시지 초기화
    disableAuthControls("emailVerifyBtn", "codeVerifyBtn")
    const email = document.getElementById("email").value;

    // GET 요청으로 이메일 중복 확인
    axios.get('/api/accounts/check-duplicate', {
        params: {email: email},  // 쿼리 파라미터로 이메일 전송
        withCredentials: true
    })
        .then(response => {
            if (response.data.status) {
                alert("사용가능한 이메일 입니다.");
                verifiedEmail = null;
                enableAuthControls("emailVerifyBtn")
            }
        })
        .catch(error => {
            if (error.response.data.data) {
                handleErrors(error.response.data.data);
            } else {
                alert(error.response.data.message);
            }
            disableAuthControls("emailVerifyBtn", "codeVerifyBtn")
        });
});

// 이메일 입력값 변경 시, 인증 초기화
document.getElementById("email").addEventListener("input", function () {
    disableAuthControls("emailVerifyBtn", "codeVerifyBtn");
});

function validateVerifiedEmail() {
    const inputEmail = document.getElementById("email").value;

    if (inputEmail !== verifiedEmail) {
        return false;
    }

    return true;
}