import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  HorseCreateEditComponent,
  HorseCreateEditMode,
} from './component/horse/horse-create-edit/horse-create-edit.component';
import { HorseComponent } from './component/horse/horse.component';
import { OwnerComponent } from './component/owner/owner.component';
import { HorseDetailComponent } from './component/horse/horse-detail/horse-detail.component';
import { OwnerAddComponent } from './component/owner/owner-add/owner-add.component';
import { FamilyTreeViewComponent } from './component/horse/family-tree-view/family-tree-view.component';

const routes: Routes = [
  { path: '', redirectTo: 'horses', pathMatch: 'full' },
  {
    path: 'owners',
    children: [
      { path: '', component: OwnerComponent },
      { path: 'create', component: OwnerAddComponent },
    ],
  },
  {
    path: 'horses',
    children: [
      { path: '', component: HorseComponent },
      { path: ':id/familytree', component: FamilyTreeViewComponent },
      { path: ':id/detail', component: HorseDetailComponent },
      {
        path: ':id/edit',
        component: HorseCreateEditComponent,
        data: { mode: HorseCreateEditMode.edit },
      },
      {
        path: 'create',
        component: HorseCreateEditComponent,
        data: { mode: HorseCreateEditMode.create },
      },
    ],
  },
  { path: '**', redirectTo: 'horses' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
