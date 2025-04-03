# 수정, 추가된 코드를 업로드 혹은 가져올 때
* 안드로이드 스튜디오 상단의 Git메뉴에서 다룰 수 있음
* 가져오기 - push를 통해서 main브랜치 내용을 가져옴
* 업로드 - 수정/추가한 내용 체크 -> commit & push를 통해서 자신의 브랜치에 업로드(가능하면 어떤 내용 수정했는지 커밋 메세지 추가)
* 깃허브 최종 업로드
    * 깃헙 페이지 -> pull request -> new pull request -> 자기 브랜치 클릭 -> create pull request
    * 혹은 깃헙 페이지 -> 자신의 브랜치로 이동 -> contribute -> open pull request -> create pull request
    * 내가 하기 줮내이 귀찮지만 리퀘스트 확인하고 master에 병합할 계획

동작 참고: https://velog.io/@prettylee620/%ED%8C%80%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EB%A5%BC-%EC%9C%84%ED%95%9C-%EA%B9%83%ED%97%88%EB%B8%8C
## 주의
* 수정된 코드들은 최종으로 master에 올라감
* 자신 이름의 브랜치에 업로드할 것
* 자잘자잘한 수정/추가는 일단 자기 브랜치에만 올리고 리퀘스트는 나중에 올릴것

## 명령어
* pull - 수정된 프로젝트를 가져옴
* commit - 수정된 프로젝트를 업로드 (개인 컴퓨터에 저장)
* push - 커밋한 프로젝트를 깃헙에 업로드
* merge - 자신 브랜치 내용을 master에 병합

명령어 참고: https://wayhome25.github.io/git/2017/07/08/git-first-pull-request-story/


# 첫 세팅(추후 삭제 예정)
## 1. 나만의 브랜치 생성하기
1. 현재 페이지에서 branch - new - 브랜치 이름 입력 ex)jiwoo
2. git - branches - new branch - 추가했던 브랜치 이름 입력
   
## 2. 첫 프로젝트 가져오기
1. 깃이 설치되지 않았다면 참고: https://dev-cini.tistory.com/16 (포스트 내용 3번 과정까지만 문제 발생시, 이후 단계 실행)
2. 현재 페이지에서 <>code버튼 클릭 후 주소 복사
3. 안드로이드 스튜디오 실행 file - new - new Project - Project from version control - 복사한 주소 입력 - clone 
