import { Component, Input } from '@angular/core';
import { HorseTree, Horse } from './../../../../dto/horse';
import { HorseService } from 'src/app/service/horse.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-tree-node',
  templateUrl: './tree-node.component.html',
  styleUrls: ['./tree-node.component.scss']
})
export class TreeNodeComponent {
  @Input() horse: HorseTree = {
    name: undefined,
    dateOfBirth: undefined,
    sex: undefined,
    generation: undefined,
    mother: undefined,
    father: undefined
  };

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private router: Router
  ) { }


  public deleteHorse(horse: HorseTree | null | undefined) {
    if( horse != null) {
       const observable: Observable<Horse> = this.service.delete(horse.id?.toString());
       observable.subscribe({
         next: data => {
           this.notification.success(`Horse ${horse.name} successfully deleted`);
           this.router.navigate(['/horses/generations/']);
         },
         error: error => {
           console.error('Error creating horse', error);
           // TODO show an error message to the user. Include and sensibly present the info from the backend!
         }
       });
     }
   }
}
