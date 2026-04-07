SELECT DISTINCT
       t2.nu_seq_candidato,
       t2.co_cpf,
       t1.nu_status_contrato,
       t1.nu_contrato,
       t1.nu_dv_contrato,
       t1.nu_sureg_agencia_contrato,
       t1.nu_unidade_contrato_fk25,
       t1.nu_operacao_siapi
  FROM fes.festb036_contrato_fies t1
 INNER JOIN fes.festb010_candidato t2
         ON t1.nu_candidato_fk11 = t2.nu_seq_candidato
 INNER JOIN fes.festb012_fiador t3
         ON t2.nu_seq_candidato = t3.nu_candidato_fk10
 WHERE t1.nu_status_contrato >= 4
   AND t1.nu_contrato IS NOT NULL
   AND t3.co_identidade IS NOT NULL
   AND NVL(t3.ic_troca_fiador, 'S') = 'S'
 ORDER BY t2.nu_seq_candidato;
