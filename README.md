
123

# 용어 정리
* pull - 수정된 프로젝트를 가져옴
* commit - 수정한 내용을 로컬 저장소에 저장(아직 깃헙(원격 저장소)에는 올라가지 않은 상태)
* push - commit된 내용을 깃헙에 업로드 (자신의 브랜치가 업데이트된걸 확인할 수 있음)
* checkout - 브랜치를 변경 후 해당 브랜치의 내용을 가져옴(함부로 하지 않는걸 추천. 내용이 어떻게 변할지 모름)
* merge - 자신 브랜치 내용을 master에 병합
* origin - 원격저장소. 깃헙을 의미함


명령어 참고: https://wayhome25.github.io/git/2017/07/08/git-first-pull-request-story/

# 수정, 추가된 코드를 업로드 혹은 가져올 때
* 안드로이드 스튜디오 상단의 Git메뉴에서 다룰 수 있음
* 가져오기 - pull를 통해서 main브랜치 내용을 가져옴
* 업로드 - 수정/추가한 내용 체크 -> commit & push를 통해서 자신의 브랜치에 업로드(가능하면 어떤 내용 수정했는지 커밋 메세지 추가)
* 깃허브 최종 업로드
    * 깃헙 페이지 -> pull request -> new pull request -> 자기 브랜치 클릭 -> create pull request
    * 혹은 깃헙 페이지 -> 자신의 브랜치로 이동 -> contribute -> open pull request -> create pull request
    * 내가 하기 줮내이 귀찮지만 리퀘스트 확인하고 master에 병합할 계획 

동작 참고: https://velog.io/@prettylee620/%ED%8C%80%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EB%A5%BC-%EC%9C%84%ED%95%9C-%EA%B9%83%ED%97%88%EB%B8%8C

# 브랜치를 추가하고싶다면 
![branch](https://github.com/user-attachments/assets/8fd41ddc-482f-46eb-8ffb-5f947f2d0ab2)

1. 프로젝트 옆에 브랜치를 다루는 탭으로 이동
2. new branch 클릭후 원하는 이름의 브랜치 생성. 아직 깃헙에는 브랜치가 생성되지않음
3. push클릭. 이제 깃헙에도 브랜치 목록이 업데이트 됨

# 주의
* 수정된 작업물들은 최종으로 master에 올라감
* 자신 이름의 브랜치에 업로드할 것
* 자잘자잘한 수정/추가는 일단 자기 브랜치에만 올리고 리퀘스트는 나중에 올릴것




