import { Component, Input, Output } from '@angular/core';
import { HorseTree } from '../../../../dto/horse';
import { EventEmitter } from '@angular/core';
import { Sex } from 'src/app/dto/sex';

@Component({
  selector: 'app-family-tree-node',
  templateUrl: './family-tree-node.component.html',
  styleUrls: ['./family-tree-node.component.scss'],
})
export class FamilyTreeNodeComponent {
  @Output() deleteEvent = new EventEmitter();
  @Input() horseTree: HorseTree = {
    id: -1,
    name: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: undefined,
    father: undefined,
  };
  isCollapsed = false;

  constructor() {}

  public collapse() {
    this.isCollapsed = !this.isCollapsed;
  }

  formatHorseName(horse: HorseTree, numberOfChars: number): string {
    if (horse == null || numberOfChars === 0) {
      return '';
    }

    if (!horse.name) {
      return '';
    }

    return horse.name.length >= numberOfChars
      ? horse.name.substring(0, numberOfChars) + '...'
      : horse.name;
  }
}
