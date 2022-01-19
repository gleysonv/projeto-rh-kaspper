import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastrarCandidatoComponent } from './cadastrar-candidato.component';

describe('CadastrarCandidatoComponent', () => {
  let component: CadastrarCandidatoComponent;
  let fixture: ComponentFixture<CadastrarCandidatoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CadastrarCandidatoComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CadastrarCandidatoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
