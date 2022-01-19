import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidatoDto} from "../../../../shared/models/dto/candidato-dto";
import {PerfilDto} from "../../../../shared/models/dto/perfil-dto";
import {CandidatoService} from "../../../../shared/services/candidato.service";
import {MensageriaService} from "../../../../shared/services/mensageria.service";
import {PerfilService} from "../../../../shared/services/perfil.service";

@Component({
  selector: 'app-cadastrar-candidato',
  templateUrl: './cadastrar-candidato.component.html',
  styleUrls: ['./cadastrar-candidato.component.scss']
})
export class CadastrarCandidatoComponent implements OnInit {

  form: FormGroup;
  listaCandidatos: CandidatoDto[] = [];
  listaPerfis: PerfilDto[] = [];
  files: any[] = [];

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private candidatoService: CandidatoService,
    private perfilService: PerfilService,
    private mensageriaService: MensageriaService) {
  }

  ngOnInit() {
    this.iniciarFormulario();
    this.candidatoService.getCandidatos().subscribe(rs => this.listaCandidatos = rs);
    this.perfilService.getPerfis().subscribe(rs => this.listaPerfis = rs);
  }

  iniciarFormulario() {
    this.form = this.formBuilder.group({
      nome: [null, [Validators.required]],
      perfil: [null, [Validators.required]],
      pretensao: [null, [Validators.required]],
      curriculo: [null, [Validators.required]],
    });

  }

  montarDadosCandidato(): CandidatoDto {
    return {
      id: null,
      nome: this.form.get('nome').value,
      perfil: this.montarPerfil(),
      pretensao : this.form.get('pretensao').value,
      curriculo: this.form.get('curriculo').value,
    };
  }

  montarPerfil(): PerfilDto {
    var perfilSelecionado = this.form.get('perfil').value;
    return this.retornarPorPerfil(perfilSelecionado);
  }

  private retornarPorPerfil(idPerfil: any) : PerfilDto {
    if (idPerfil === 1) {
      return {
        id: this.listaPerfis[0].id,
        descricao: this.listaPerfis[0].descricao,
        observacao: this.listaPerfis[0].observacao,
      };
    } else if (idPerfil === 2) {
      return {
        id: this.listaPerfis[1].id,
        descricao: this.listaPerfis[1].descricao,
        observacao: this.listaPerfis[1].observacao,
      };
    } else {
      return {
        id: this.listaPerfis[2].id,
        descricao: this.listaPerfis[2].descricao,
        observacao: this.listaPerfis[2].observacao,
      };
    }
  }


  async create(candidato: CandidatoDto) {
    candidato = this.montarDadosCandidato();
    if (this.form.valid) {
      this.candidatoService.salvar(candidato).subscribe(() => {
        this.mensageriaService.showMensagemSucesso('Salvo com Sucesso.')
      }, error => {
        this.mensageriaService.showMensagemInformativa(error.message);
      }, () => {
        this.form.reset();
        Object.keys(this.form.controls).forEach(key => {
          this.form.get(key).setErrors(null);
        });

        this.goToPesquisar();
      });
    }
  }

  hasErros(controlName: string, errorName: string): any {
    return this.form.controls[controlName].hasError(errorName);
  }

  goToHome(): void {
    this.router.navigate([`/`]);
  }

  private goToPesquisar() {
    this.router.navigate([`listar-candidato`]);
  }
}
