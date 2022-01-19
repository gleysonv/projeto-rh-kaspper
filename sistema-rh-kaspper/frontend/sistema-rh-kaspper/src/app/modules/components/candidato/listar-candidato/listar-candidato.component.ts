import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidatoDto} from "../../../../shared/models/dto/candidato-dto";
import {CandidatoService} from "../../../../shared/services/candidato.service";
import {MensageriaService} from "../../../../shared/services/mensageria.service";
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";

@Component({
  selector: 'app-listar-candidato',
  templateUrl: './listar-candidato.component.html',
  styleUrls: ['./listar-candidato.component.scss']
})
export class ListarCandidatoComponent implements OnInit {

  public colunasCandidato = ['nome', 'perfil', 'pretensao', 'cv'];
  public candidatoDataSource: CandidatoDto[] = [];
  datasource: MatTableDataSource<CandidatoDto>;
  listaCandidatos: CandidatoDto[] = [];
  private paginacao: any;

  @Output() SubmitEvent = new EventEmitter<object>();
  @ViewChild(MatPaginator) clientePaginator: MatPaginator;

  flagTable = true;

  form: FormGroup;

  // Controle da Tabela
  totalResultados = 0;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private candidatoService: CandidatoService,
    private mensageriaService: MensageriaService) {

  }

  ngOnInit() {
    this.iniciarFormulario();
    // Lista todos os Candidatos ao iniciar a tela.
    this.candidatoService.getCandidatos().subscribe(rs => this.listaCandidatos = rs)
    this.getCandidatos();
  }

  iniciarFormulario() {
    this.form = this.formBuilder.group({
      vaga: [null, [Validators.required]],
      candidatos: [null, [Validators.required]],

    });

  }
  configuraPaginaCliente($event: PageEvent) {

  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.datasource.filter = filterValue.trim().toLowerCase();
  }

  async getCandidatos() {
    this.candidatoService.getCandidatos().subscribe(data => {
      this.totalResultados = data.length;
      if (this.totalResultados) {
        this.candidatoDataSource = data;
        this.paginacao = data;
        this.datasource = new MatTableDataSource(data);
        this.flagTable = true;
      } else {
        this.flagTable = false;
      }
    }, error => {
      this.mensageriaService.showMensagemErro(error.mensagem);
    }, () => {
      this.form.reset();
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key).setErrors(null);
      });
    });
  }

}
