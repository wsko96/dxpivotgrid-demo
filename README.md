# dxpivotgrid-demo

�����ͽ������� �Ǻ��׸����� �Ʒ� �� ���� �ɼ� [1] ���� ��:
1. Local Operations: tabular�� json �����ͼ��� Ŭ���̾�Ʈ�� �ε��� ����, �Ǻ��׸��忡 �׳� �����ϰ� �Ǻ��׸��尡 Ŭ���̾�Ʈ ���̵忡�� aggregation�� ��� �ϴ� ���.
2. Remote Operations: `remoteOperation` �ɼ��� �Ѱ�, �⺻ ������ URL�� �����ϸ�, �Ǻ��׸��尡 aggregation ��û���� �Ķ���͵�� �Բ� �ټ� ȣ���ϴ� ���. ��, ���� ���̵忡�� aggregation ����

[1] https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/

## ���� �� ����

    set MAVEN_OPTS=-Xmx1024m
    mvn clean verify jetty:run

�׸��� http://localhost:8080 �� �湮�Ͻʽÿ�.

## ���� Sales Data REST API

    http://localhost:8080/api/v1/sales

- ���� �׽�Ʈ ������: �����ͽ������� �Ǻ��׸��� ���� ���ԵǾ� �ִ� `sales.js` ������
  [src/main/resources/kr/wise/demo/pivotgrid/repository/sales.json](src/main/resources/kr/wise/demo/pivotgrid/repository/sales.json) ���Ϸ� ������ ��,
  [src/main/java/kr/wise/demo/pivotgrid/repository/SalesDataRepository.java](src/main/java/kr/wise/demo/pivotgrid/repository/SalesDataRepository.java) ����, ���������� �鸸 �� �̻����� �����Ͽ� �׽�Ʈ ������ ����.

- REST Service �ҽ�: [src/main/java/kr/wise/demo/pivotgrid/service/SalesDataService.java](src/main/java/kr/wise/demo/pivotgrid/service/SalesDataService.java)

- �Ϻ� raw �����͸� �ޱ� [1]
  ��) http://localhost:8080/api/v1/sales?take=20

- Aggregation ȣ�� URL���� ���� ���� [1] ��, ������ ��Ʈ��ũ ����� ����.