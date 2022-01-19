import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidatoDto} from "../../../../shared/models/dto/candidato-dto";
import {Router} from "@angular/router";
import {CandidatoService} from "../../../../shared/services/candidato.service";
import {MensageriaService} from "../../../../shared/services/mensageria.service";
import {PerfilDto} from "../../../../shared/models/dto/perfil-dto";
import {VagaDto} from "../../../../shared/models/dto/vaga-dto";
import {VagaService} from "../../../../shared/services/vaga.service";

@Component({
  selector: 'app-cadastrar-vaga',
  templateUrl: './cadastrar-vaga.component.html',
  styleUrls: ['./cadastrar-vaga.component.scss']
})
export class CadastrarVagaComponent implements OnInit {
  form: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private vagaService: VagaService,
    private mensageriaService: MensageriaService) {
  }

  ngOnInit() {
    this.iniciarFormulario();
  }

  iniciarFormulario() {
    this.form = this.formBuilder.group({
      descricao: [null, [Validators.required]],
      tipo: [null, [Validators.required]],
      salario: [null, [Validators.required]],
    });

  }

  montarDadosVaga(): VagaDto {
    return {
      id: null,
      descricao: this.form.get('descricao').value,
      tipo : this.form.get('tipo').value,
      salario : this.form.get('salario').value,
      perfil: this.montarPerfil(),

    };
  }

  montarPerfil(): PerfilDto {
    return {
      id: null,
      descricao: this.form.get('perfil').value,
      observacao: null,
    };
  }

  async create(vaga: VagaDto) {
    vaga = this.montarDadosVaga();
    if (this.form.valid) {
      this.vagaService.salvar(vaga).subscribe(() => {
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
