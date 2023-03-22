import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse, HorseSearch} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  styleUrls: ['./horse-detail.component.scss']
})
export class HorseDetailComponent {

  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    mother: undefined,
    father: undefined
  };

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  ngOnInit(): void {
      this.route.paramMap.subscribe( paramMap => {
        //id should always be present, because if not this route is not accesible
        const id = paramMap.get('id');
        this.service.getById(id).subscribe((response: Horse) => {
          this.horse = response;
        });
      });
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatHorseName(horse: Horse | null | undefined): string {
    return (horse == null)
      ? ''
      : horse.name;
  }

  public deleteHorse(horse: Horse | null | undefined) {
    if( horse != null) {
       const observable: Observable<Horse> = this.service.delete(horse.id?.toString());
       observable.subscribe({
         next: data => {
           this.notification.success(`Horse ${horse.name} successfully deleted`);
           this.router.navigate(['/horses']);
         },
         error: error => {
           console.error('Error creating horse', error);
           // TODO show an error message to the user. Include and sensibly present the info from the backend!
         }
       });
     }
   }
}
