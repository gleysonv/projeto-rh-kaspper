import { BrowserModule } from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {SharedModule} from './shared/models/shared.module';
import { CadastrarVagaComponent } from './modules/components/vaga/cadastrar-vaga/cadastrar-vaga.component';
import { CadastrarCandidatoComponent } from './modules/components/candidato/cadastrar-candidato/cadastrar-candidato.component';
import { ListarCandidatoComponent } from './modules/components/candidato/listar-candidato/listar-candidato.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatNativeDateModule, MatOptionModule} from "@angular/material/core";
import {MatSelectModule} from "@angular/material/select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";
import {MatDividerModule} from "@angular/material/divider";
import {MatInputModule} from "@angular/material/input";
import {RouterModule} from "@angular/router";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatRadioModule} from "@angular/material/radio";
import {NgxMatFileInputModule} from "@angular-material-components/file-input";

@NgModule({
  declarations: [
    AppComponent,
    CadastrarVagaComponent,
    CadastrarCandidatoComponent,
    ListarCandidatoComponent,
  ],
  imports: [
    AppRoutingModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MatFormFieldModule,
    MatDividerModule,
    MatCardModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatListModule,
    MatNativeDateModule,
    MatSelectModule,
    RouterModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatOptionModule,
    MatPaginatorModule,
    MatTableModule,
    MatRadioModule,
    SharedModule,
    FlexLayoutModule,
    NgxMatFileInputModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule { }
