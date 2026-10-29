-- Execute no banco H2 depois de rodar a aplicacao.
SELECT * FROM marca;
SELECT * FROM carro;
SELECT * FROM acessorio;
SELECT * FROM carro_acessorio;

SELECT c.modelo, c.ano, m.nome AS marca, a.nome AS acessorio
FROM carro c
JOIN marca m ON m.id = c.marca_id
LEFT JOIN carro_acessorio ca ON ca.carro_id = c.id
LEFT JOIN acessorio a ON a.id = ca.acessorio_id
ORDER BY c.id, a.nome;

SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC';
SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = 'PUBLIC';
