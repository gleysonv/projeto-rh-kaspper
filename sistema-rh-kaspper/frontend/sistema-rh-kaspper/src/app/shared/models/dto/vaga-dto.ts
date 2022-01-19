import {PerfilDto} from "./perfil-dto";

export interface VagaDto {
  id?: number;
  descricao: string;
  tipo : number;
  salario : number;
  perfil: PerfilDto;
}
