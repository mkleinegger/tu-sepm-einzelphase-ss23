import { Owner } from './owner';
import { Sex } from './sex';

export interface Horse {
  id?: number;
  name: string;
  description?: string;
  dateOfBirth: Date;
  sex: Sex;
  owner?: Owner;
  mother?: Horse;
  father?: Horse;
}

export interface HorseTree {
  id: number;
  name: string;
  dateOfBirth: Date;
  sex: Sex;
  mother?: HorseTree;
  father?: HorseTree;
}

export interface HorseSearch {
  name?: string;
  description?: string;
  bornBefore?: Date;
  sex?: Sex;
  ownerName?: string;
  limit?: number;
}
