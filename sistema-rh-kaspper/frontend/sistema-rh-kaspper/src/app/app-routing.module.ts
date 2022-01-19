import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {
  CadastrarCandidatoComponent
} from "./modules/components/candidato/cadastrar-candidato/cadastrar-candidato.component";
import {ListarCandidatoComponent} from "./modules/components/candidato/listar-candidato/listar-candidato.component";
import {CadastrarVagaComponent} from "./modules/components/vaga/cadastrar-vaga/cadastrar-vaga.component";

const routes: Routes = [
  {path: 'cadastrar-candidato', component: CadastrarCandidatoComponent},
  {path: 'cadastrar-vaga', component: CadastrarVagaComponent},
  {path: 'listar-candidato', component: ListarCandidatoComponent},
  {path: '', redirectTo: '/', pathMatch: 'full'}];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
