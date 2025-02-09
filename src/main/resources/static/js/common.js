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