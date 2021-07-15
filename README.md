# dxpivotgrid-demo

데브익스프레스 피봇그리드의 아래 두 가지 옵션 [1] 성능 비교:
1. Local Operations: tabular한 json 데이터셋을 클라이언트가 로드한 다음, 피봇그리드에 그냥 전달하고 피봇그리드가 클라이언트 사이드에서 aggregation을 모두 하는 방식.
2. Remote Operations: `remoteOperation` 옵션을 켜고, 기본 데이터 URL을 설정하면, 피봇그리드가 aggregation 요청들을 파라미터들과 함께 다수 호출하는 방식. 즉, 서버 사이드에서 aggregation 수행

[1] https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/

## 빌드 및 실행

    set MAVEN_OPTS=-Xmx1024m
    mvn clean verify jetty:run

그리고 http://localhost:8080 를 방문하십시오.

## 예제 Sales Data REST API

    http://localhost:8080/api/v1/sales

- 예제 테스트 데이터: 데브익스프레스 피봇그리드 데모에 포함되어 있는 `sales.js` 파일을
  [src/main/resources/kr/wise/demo/pivotgrid/repository/sales.json](src/main/resources/kr/wise/demo/pivotgrid/repository/sales.json) 파일로 변형한 뒤,
  [src/main/java/kr/wise/demo/pivotgrid/repository/SalesDataRepository.java](src/main/java/kr/wise/demo/pivotgrid/repository/SalesDataRepository.java) 에서, 인위적으로 백만 건 이상으로 변형하여 테스트 데이터 구성.

- REST Service 소스: [src/main/java/kr/wise/demo/pivotgrid/service/SalesDataService.java](src/main/java/kr/wise/demo/pivotgrid/service/SalesDataService.java)

- 일부 raw 데이터만 받기 [1]
  예) http://localhost:8080/api/v1/sales?take=20

- Aggregation 호출 URL들은 관련 문서 [1] 와, 브라우저 네트워크 디버거 참조.