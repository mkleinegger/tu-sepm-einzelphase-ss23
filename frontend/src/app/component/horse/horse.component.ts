import {Component, OnInit} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse, HorseSearch} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {Observable} from 'rxjs';


@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  search = false;
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseSearch: HorseSearch = {
    name: undefined,
    description: undefined,
    bornBefore: undefined,
    sex: undefined,
    ownerName: undefined,
    limit: undefined
  };

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadHorses();
  }

  public deleteHorse(horse: Horse | null | undefined) {
    if( horse != null) {
       const observable: Observable<Horse> = this.service.delete(horse.id?.toString());
       observable.subscribe({
         next: data => {
           this.notification.success(`Horse ${horse.name} successfully deleted`);
           this.reloadHorses();
         },
         error: error => {
           console.error('Error creating horse', error);
           // TODO show an error message to the user. Include and sensibly present the info from the backend!
         }
       });
     }
   }

  reloadHorses() {
    this.service.search(this.horseSearch)
      .subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

}
