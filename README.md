## :interrobang:  수정 사항
- 1:n 채팅방 생성 로직 추가
	- 엔드포인트(path) 변경은 없음
	- 넘어오는 memberId 의 갯수 따라 자동적으로 1:1 혹은 1:n 처리
		- ex )
		```json
		{ "memberIds":[1,2] } // memberId 2개로 request ->  1:1 채팅 로직 적용
		{ "memberIds":[1,2,3] } // memberId 3개이상으로 request -> 그룹 채팅 로직 적용
		```

- AuthService.getLoginMember 로직을 spring security 의 @AuthenticationPrincipal 로 대체

## :white_check_mark: TO DO
- Amazon MQ를 통한 rabbitMQ 도입

## :construction: Commit Convention
- add : 새로운 기능 추가
- fix : 버그 수정
- docs : 문서 수정
- style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
- refactor : 코드 리펙토링
- test : 테스트 코드, 리펙토링 테스트 코드 추가
- chore : 빌드 업무 수정, 패키지 매니저 수정
