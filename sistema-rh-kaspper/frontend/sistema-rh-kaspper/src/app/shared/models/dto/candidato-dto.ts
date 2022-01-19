import {PerfilDto} from "./perfil-dto";

export interface CandidatoDto {
  id?: number;
  nome?: string;
  perfil?: PerfilDto;
  pretensao: number;
  curriculo: any;
}
